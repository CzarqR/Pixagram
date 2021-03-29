package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.databinding.FragmentHomeBinding
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.ui.fragments.utils.FragmentPostRecycler
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.vm.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : FragmentPostRecycler(R.layout.fragment_home)
{
    override val viewModel: HomeViewModel by activityViewModels()
    override val binding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        setupRecycler()
    }

    private fun setupRecycler()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.postsFromFollowingUsers.collectLatest {
                Timber.d("Collecting posts from following users: $it")

                val data = it.toList()
                postAdapter.submitList(data)
                setState(data.isEmpty())
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.arePostsLoading.collectLatest {
                binding.proBarLoadingPosts.isVisible = it
                setState(false)
            }
        }
    }

    private fun setState(isListEmpty: Boolean)
    {
        // show state only when data is not loading
        if (!viewModel.arePostsLoading.value)
        {
            binding.rvPosts.isVisible = !isListEmpty
            binding.imgIconFeed.isVisible = isListEmpty
            binding.txtNothingToShow.isVisible = isListEmpty
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
        val action = HomeFragmentDirections.actionHomeFragmentToCommentFragment(
            postId = postId
        )
        findNavController().navigate(action)
    }

    override fun imageClick(postWithId: PostWithId)
    {
        val action = HomeFragmentDirections.actionHomeFragmentToDetailPostFragment(
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
            val action = HomeFragmentDirections.actionHomeFragmentToUserFragment(
                user = User(username = mention),
                loadUserFromDb = true
            )
            findNavController().navigate(action)
        }
    }

    override fun tagClick(tag: String)
    {
        val action = HomeFragmentDirections.actionHomeFragmentToTagFragment(
            tag = Tag(tag, -1),
        )
        findNavController().navigate(action)
    }

    // endregion
}