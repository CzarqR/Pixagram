package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
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

        postAdapter.commentListener = ::commentClick
        postAdapter.profileListener = ::profileClick
        postAdapter.imageListener = ::imageClick
        postAdapter.linkListener = ::linkClick
        postAdapter.mentionListener = ::mentionClick
        postAdapter.tagListener = ::tagClick

        binding.rvPosts.adapter = postAdapter

        lifecycleScope.launchWhenStarted {
            viewModel.postsFromFollowingUsers.collectLatest {
                Timber.d("Collecting posts from following users: $it")
                postAdapter.submitList(it.toList())
            }
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


    override fun onDetach()
    {
        super.onDetach()
        Timber.d("onDestroy detach")
        postAdapter.cancelScopes()
    }
}