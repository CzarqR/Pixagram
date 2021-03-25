package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.adapters.postadapter.PostClickListener
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.databinding.FragmentHomeBinding
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.vm.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home)
{
    @Inject
    lateinit var postAdapter: PostAdapter

    private val viewModel: HomeViewModel by activityViewModels()
    private val binding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        setupRecycler()
    }

    private fun setupRecycler()
    {
        postAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

        postAdapter.postClickListener = PostClickListener(
            commentListener = ::commentClick,
            profileListener = ::profileClick,
            imageListener = ::imageClick,
            linkListener = ::linkClick,
            mentionListener = ::mentionClick,
            tagListener = ::tagClick,
            likeListener = ::likePost
        )

        binding.rvPosts.adapter = postAdapter

        registerForContextMenu(binding.rvPosts)

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


    private fun profileClick(postOwner: String)
    {
        if (viewModel.isOwnAccount(postOwner)) // user clicked on own profile (currently impossible because there are no own post on home feed)
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

    private fun commentClick(postId: String)
    {
        val action = HomeFragmentDirections.actionHomeFragmentToCommentFragment(
            postId = postId
        )
        findNavController().navigate(action)
    }

    private fun imageClick(post: PostWithId)
    {
        val action = HomeFragmentDirections.actionHomeFragmentToDetailPostFragment(
            post = post.second,
            postId = post.first
        )
        findNavController().navigate(action)
    }

    private fun linkClick(link: String)
    {
        Timber.d("Link clicked $link")
    }

    private fun mentionClick(mention: String)
    {
        Timber.d("Mention clicked $mention")
    }

    private fun tagClick(tag: String)
    {
        Timber.d("Tag clicked $tag")
        val action = HomeFragmentDirections.actionHomeFragmentToTagFragment(
            tag = Tag(tag, -1),
        )
        findNavController().navigate(action)
    }

    private fun likePost(postId: String, status: Boolean)
    {
        viewModel.setLikeStatus(postId, status)
    }

    /**
     * When View is destroyed adapter should cancel scope in every ViewHolder
     */
    override fun onDestroyView()
    {
        super.onDestroyView()
        postAdapter.cancelScopes()
    }


}