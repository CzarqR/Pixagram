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
import com.myniprojects.pixagram.databinding.FragmentHomeBinding
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

        postAdapter.commentListener = { postId ->
            val action = HomeFragmentDirections.actionHomeFragmentToCommentFragment(
                postId = postId
            )
            findNavController().navigate(action)
        }

        postAdapter.profileListener = { postOwner ->

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

        binding.rvPosts.adapter = postAdapter

        lifecycleScope.launchWhenStarted {
            viewModel.postsFromFollowingUsers.collectLatest {
                Timber.d("Collecting posts from following users: $it")
                postAdapter.submitList(it.toList())
            }
        }
    }
}