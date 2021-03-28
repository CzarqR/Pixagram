package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.databinding.FragmentLikedBinding
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.DataStatus
import com.myniprojects.pixagram.vm.LikedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class LikedFragment : Fragment(R.layout.fragment_liked)
{
    private val binding by viewBinding(FragmentLikedBinding::bind)
    private val viewModel: LikedViewModel by viewModels()

    @Inject
    lateinit var postAdapter: PostAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
    }

    private fun setupRecycler()
    {
        binding.rvLikedPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLikedPosts.layoutManager!!.scrollToPosition(0)

//        postAdapter.postClickListener = PostClickListener(
//            commentClick = ::commentClick,
//            profileClick = ::profileClick
//        )


        binding.rvLikedPosts.adapter = postAdapter

        lifecycleScope.launchWhenStarted {
            viewModel.getLikedPostByLoggedUser().collectLatest {

                when (it)
                {
                    is DataStatus.Failed ->
                    {
                        Timber.d("Loading liked post by logged user Failed")
                    }
                    DataStatus.Loading ->
                    {
                        Timber.d("Loading liked post by logged user")
                    }
                    is DataStatus.Success ->
                    {
                        postAdapter.submitList(it.data.toList())
                    }
                }

                Timber.d("Collecting posts from following users: $it")
            }
        }
    }

    private fun commentClick(postId: String)
    {
        val action = LikedFragmentDirections.actionLikedFragmentToCommentFragment(
            postId = postId
        )
        findNavController().navigate(action)
    }

    private fun profileClick(postOwner: String)
    {

        if (viewModel.isOwnAccount(postOwner)) // user clicked on own profile (currently impossible because there are no own post on home feed)
        {
            findNavController().navigate(R.id.profileFragment)
        }
        else
        {
            val action = LikedFragmentDirections.actionLikedFragmentToUserFragment(
                user = User(id = postOwner),
                loadUserFromDb = true
            )
            findNavController().navigate(action)
        }
    }

}