package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.adapters.searchadapter.SearchModelAdapter
import com.myniprojects.pixagram.databinding.FragmentSearchBinding
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.utils.ext.*
import com.myniprojects.pixagram.utils.status.DataStatus
import com.myniprojects.pixagram.utils.status.SearchStatus
import com.myniprojects.pixagram.vm.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class SearchFragment : Fragment(R.layout.fragment_search)
{
    private val binding by viewBinding(FragmentSearchBinding::bind)
    private val viewModel: SearchViewModel by activityViewModels()

    @Inject
    lateinit var searchModelAdapter: SearchModelAdapter

    @Inject
    lateinit var postAdapter: PostAdapter

    private lateinit var menuItemSearch: MenuItem
    private lateinit var menuItemSearchType: MenuItem
    private lateinit var searchView: SearchView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        setupAdapters()
        setupRecycler()
    }

    private var areRecommendedPostsLoading = false

    private fun setupCollecting()
    {
        /**
         * Collect recommended posts
         */
        lifecycleScope.launchWhenStarted {
            viewModel.recommendedPosts.collectLatest { dataStatus ->

                when (dataStatus)
                {
                    is DataStatus.Failed ->
                    {
                        binding.progressBarPosts.isVisible = false
                        areRecommendedPostsLoading = false
                    }
                    DataStatus.Loading ->
                    {
                        binding.progressBarPosts.isVisible = true
                        areRecommendedPostsLoading = true
                    }
                    is DataStatus.Success ->
                    {
                        /**
                         * Sorting in client side, recommended posts is a small list (at this time)
                         */
                        postAdapter.submitList(dataStatus.data.toList().sortedByDescending {
                            it.second.time
                        })
                        binding.progressBarPosts.isVisible = false
                        areRecommendedPostsLoading = false
                    }
                }.exhaustive
            }
        }

        /**
         * Collect search type
         */
        lifecycleScope.launchWhenStarted {
            viewModel.currentSearchType.collectLatest {
                setSearchIcon(it)
                search(searchView.query.toString())
            }
        }
    }

    private var searchJob: Job? = null

    /**
     * When query is empty app shows latest post
     */
    private fun search(query: String)
    {
        if (isFragmentAlive)
        {
            if (query.isEmpty())
            {
                displayEmptyResult(false)

                Timber.d("Empty")
                if (binding.rvSearch.adapter != postAdapter)
                {
                    binding.rvSearch.adapter = postAdapter

                    /**
                     * if posts are still loading show ProgressBar again
                     */
                    binding.progressBarPosts.isVisible = areRecommendedPostsLoading
                }
            }
            else
            {
                binding.rvSearch.adapter = searchModelAdapter

                /**
                 * hide ProgressBar for recommended posts
                 */
                binding.progressBarPosts.isVisible = false

                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    viewModel.search(query).collectLatest {
                        when (it)
                        {
                            is SearchStatus.Interrupted ->
                            {
                                binding.progressBarSearch.isVisible = false
                                displayEmptyResult(false)
                            }
                            SearchStatus.Loading ->
                            {
                                binding.progressBarSearch.isVisible = true
                                displayEmptyResult(false)
                            }
                            is SearchStatus.Success ->
                            {
                                binding.progressBarSearch.isVisible = false
                                searchModelAdapter.submitList(it.result)
                                displayEmptyResult(it.result.isEmpty())
                            }
                        }.exhaustive
                    }
                }
            }
        }
    }

    private fun displayEmptyResult(show: Boolean)
    {
        binding.txtEmptyResult.isVisible = show
        binding.imgEmptyResult.isVisible = show
    }

    private fun setupAdapters()
    {
        searchModelAdapter.apply {
            userListener = ::selectUser
            tagListener = ::selectTag
        }

        /**
         * In future maybe it will be better to change list look.
         * Now it looks the same as home feed
         */
        postAdapter.apply {
            //TODO setup post adapter
        }
    }

    private fun selectUser(user: User)
    {
        if (Firebase.auth.currentUser?.uid == user.id)
        {
            findNavController().navigate(R.id.profileFragment)
        }
        else
        {
            val action = SearchFragmentDirections.actionSearchFragmentToUserFragment(
                user = user
            )
            findNavController().navigate(action)
        }
    }

    private fun selectTag(tag: Tag)
    {
        val action = SearchFragmentDirections.actionSearchFragmentToTagFragment(
            tag = tag
        )
        findNavController().navigate(action)
    }


    private fun setupRecycler()
    {
        with(binding.rvSearch)
        {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_toolbar_search, menu)

        menuItemSearch = menu.findItem(R.id.itemSearch)
        menuItemSearchType = menu.findItem(R.id.itemSearchType)
        searchView = menuItemSearch.actionView as SearchView

        setupCollecting()

        viewModel.currentQuery?.let {
            menuItemSearch.expandActionView()
            searchView.setQuery(it, false)
            searchView.clearFocus()
            search(it)
        }

        /**
         * currently search is fired when text is changing
         * if it will be slow make search only after submitting query
         */
        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener
            {
                override fun onQueryTextSubmit(textInput: String?): Boolean
                {
                    hideKeyboard()
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean
                {
                    search(query)
                    return true
                }

            }
        )

        menuItemSearch.setOnActionExpandListener(
            object : MenuItem.OnActionExpandListener
            {
                override fun onMenuItemActionExpand(p0: MenuItem?): Boolean
                {
                    return true
                }

                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean
                {
                    return true
                }
            }
        )
    }

    private fun setSearchIcon(searchType: SearchType)
    {
        val d = ContextCompat.getDrawable(requireContext(), searchType.icon)
        if (d != null)
        {
            DrawableCompat.setTint(
                d,
                ContextCompat.getColor(requireContext(), R.color.icon_tint_toolbar)
            )
            menuItemSearchType.icon = d
        }

        menuItemSearchType.title = getString(searchType.title)

        setActionBarTitle(searchType.actionBarTitle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            R.id.itemSearchType ->
            {
                viewModel.selectNextSearchType()
                true
            }
            else ->
            {
                super.onOptionsItemSelected(item)
            }
        }
    }

    /**
     * If query is empty clear query in vm
     * in future try to put query in vm, should be easier
     */
    override fun onStop()
    {
        super.onStop()
        if (searchView.query.isEmpty())
        {
            viewModel.clearQuery()
        }
    }

    enum class SearchType(
        @DrawableRes val icon: Int,
        @StringRes val title: Int,
        @StringRes val actionBarTitle: Int,
    )
    {
        USER(
            R.drawable.ic_outline_person_outline_24,
            R.string.user,
            R.string.find_user,
        ),
        TAG(
            R.drawable.ic_outline_tag_24,
            R.string.tag,
            R.string.search_by_tags,
        )
    }
}
