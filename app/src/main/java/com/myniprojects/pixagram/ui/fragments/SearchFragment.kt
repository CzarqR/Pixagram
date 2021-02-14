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
import com.myniprojects.pixagram.adapters.searchadapter.SearchModelAdapter
import com.myniprojects.pixagram.databinding.FragmentSearchBinding
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.ext.hideKeyboard
import com.myniprojects.pixagram.utils.ext.isFragmentAlive
import com.myniprojects.pixagram.utils.ext.viewBinding
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

    private lateinit var menuItemSearch: MenuItem
    private lateinit var searchView: SearchView

    private val searchTypesArray = enumValues<SearchType>()
    private var selectedSearchTypeIndex = 0

    private fun selectNextSearchType()
    {
        selectedSearchTypeIndex++
        selectedSearchTypeIndex %= searchTypesArray.size
        search(searchView.query.toString())
    }

    private val currentSearchType: SearchType
        get() = searchTypesArray[selectedSearchTypeIndex]


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


    private var searchJob: Job? = null

    private fun search(query: String)
    {
        if (isFragmentAlive)
        {
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                viewModel.search(query, currentSearchType).collectLatest {
                    Timber.d("Collected NEW $it")
                    when (it)
                    {
                        is SearchStatus.Interrupted ->
                        {
                            binding.progressBarSearch.isVisible = false
                        }
                        SearchStatus.Loading ->
                        {
                            binding.progressBarSearch.isVisible = true
                        }
                        is SearchStatus.Success ->
                        {
                            binding.progressBarSearch.isVisible = false
                            searchModelAdapter.submitList(it.result)
                        }
                    }.exhaustive
                }
            }
        }
    }


    private fun setupAdapters()
    {
        searchModelAdapter.userListener = ::selectUser
        searchModelAdapter.tagListener = ::selectTag
    }

    private fun selectUser(user: User)
    {
        Timber.d("Clicked")
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
            adapter = searchModelAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_toolbar_search, menu)

        menuItemSearch = menu.findItem(R.id.itemSearch)
        searchView = menuItemSearch.actionView as SearchView

        // change searchText if it has been set. TODO
        // viewModel.queryFlow.value?.let {
        //     menuItemSearch!!.expandActionView()
        //     searchView!!.setQuery(it, false)
        //     searchView!!.clearFocus()
        // }

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
                    //textInput?.let { query ->
                    //    search(query)
                    //}
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean
                {
                    Timber.d("Text changed")
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            R.id.itemSearchType ->
            {
                selectNextSearchType()
                val d = ContextCompat.getDrawable(requireContext(), currentSearchType.icon)
                if (d != null)
                {
                    DrawableCompat.setTint(
                        d,
                        ContextCompat.getColor(requireContext(), R.color.icon_tint_toolbar)
                    )
                    item.icon = d
                }

                item.title = getString(currentSearchType.title)
                true
            }
            else ->
            {
                super.onOptionsItemSelected(item)
            }
        }
    }


    enum class SearchType(
        @DrawableRes val icon: Int,
        @StringRes val title: Int
    )
    {
        USER(
            R.drawable.ic_outline_person_outline_24,
            R.string.user
        ),
        TAG(
            R.drawable.ic_outline_tag_24,
            R.string.tag
        )
    }
}
