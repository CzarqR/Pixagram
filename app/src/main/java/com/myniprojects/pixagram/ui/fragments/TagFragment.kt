package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.databinding.FragmentTagBinding
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.ui.fragments.utils.FragmentPostRecycler
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.ext.isEqualTo
import com.myniprojects.pixagram.utils.ext.setActionBarTitle
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.DataStatus
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.vm.TagViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TagFragment : FragmentPostRecycler(R.layout.fragment_tag)
{
    @Inject
    lateinit var glide: RequestManager

    override val binding by viewBinding(FragmentTagBinding::bind)
    override val viewModel: TagViewModel by activityViewModels()

    private val args: TagFragmentArgs by navArgs()

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initTag(args.tag)
        setActionBarTitle(getString(R.string.tag_title_format, args.tag.title))
        setupCollecting()
        setupRecycler()
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.tag.collectLatest {

                when (it)
                {
                    is GetStatus.Failed ->
                    {
                    }
                    GetStatus.Loading ->
                    {
                        with(binding)
                        {
                            txtPosts.text = getString(R.string.loading_posts_counter)
                        }
                    }
                    is GetStatus.Success ->
                    {
                        with(binding)
                        {
                            txtPosts.text = resources.getQuantityString(
                                R.plurals.tag_counter_post,
                                it.data.count.toInt(),
                                it.data.count
                            )
                        }
                    }
                }.exhaustive


            }
        }
    }

    /**
     * Todo display properly: loading / error / empty list
     */
    private fun setupRecycler()
    {
        /**
         * Collect selected user posts
         */
        lifecycleScope.launchWhenStarted {
            viewModel.posts.collectLatest { postsStatus ->
                Timber.d(postsStatus.toString())

                when (postsStatus)
                {
                    DataStatus.Loading ->
                    {
                    }
                    is DataStatus.Success ->
                    {
                        val l = postsStatus.data.toList().sortedByDescending {
                            it.second.time
                        }
//                        postAdapter.submitList(l)

                        if (l.isNotEmpty())
                        {
                            glide
                                .load(l[0].second.imageUrl)
                                .into(binding.imgTag)
                        }
                    }
                    is DataStatus.Failed ->
                    {
                    }
                }
            }
        }
    }

    // region post callbacks

    override fun profileClick(postOwner: String)
    {
        if (viewModel.isOwnAccountId(postOwner)) // user clicked on own profile (currently impossible because there are no own post on home feed)
        {
            findNavController().navigate(R.id.profileFragment)
        }
        else
        {
            val action = HomeFragmentDirections.actionHomeFragmentToUserFragment(
                user = User(id = postOwner),
                loadUserFromDb = true
            )
            findNavController().navigate(action)
        }
    }

    override fun commentClick(postId: String)
    {
        val action = TagFragmentDirections.actionTagFragmentToCommentFragment(
            postId = postId
        )
        findNavController().navigate(action)
    }

    override fun imageClick(postWithId: PostWithId)
    {
        val action = TagFragmentDirections.actionTagFragmentToDetailPostFragment(
            post = postWithId.second,
            postId = postWithId.first
        )
        findNavController().navigate(action)
    }


    override fun mentionClick(mention: String)
    {
        if (viewModel.isOwnAccountUsername(mention)) // user clicked on own profile
        {
            findNavController().navigate(R.id.profileFragment)
        }
        else
        {
            val action = TagFragmentDirections.actionTagFragmentToUserFragment(
                user = User(username = mention),
                loadUserFromDb = true
            )
            findNavController().navigate(action)
        }
    }

    override fun tagClick(tag: String)
    {
        if (tag isEqualTo (viewModel.tag.value as? GetStatus.Success<Tag>)?.data?.title) // same tag was clicked
        {
            showSnackbar(R.string.you_are_currenly_on_this_tag)
        }
        else
        {
            val action = TagFragmentDirections.actionTagFragmentSelf(
                tag = Tag(tag, -1),
            )
            findNavController().navigate(action)
        }
    }

    // endregion

}