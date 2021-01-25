package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentUserBinding
import com.myniprojects.pixagram.utils.setActionBarTitle
import com.myniprojects.pixagram.utils.viewBinding
import com.myniprojects.pixagram.vm.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class UserFragment : Fragment(R.layout.fragment_user)
{
    @Inject
    lateinit var glide: RequestManager

    private val binding by viewBinding(FragmentUserBinding::bind)
    private val viewModel: UserViewModel by viewModels()

    private val args: UserFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initUser(args.user)

        setupCollecting()
        setupClickListeners()
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.selectedUser.collectLatest {
                if (it != null)
                {
                    with(binding)
                    {
                        txtDesc.text = it.bio
                        txtFullName.text = it.fullName

                        glide
                            .load(it.imageUrl)
                            .into(imgAvatar)
                    }
                    setActionBarTitle(it.username)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.selectedUserFollowedBy.collectLatest { followers ->
                binding.txtCounterFollowers.text = followers.count().toString()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.selectedUserFollowing.collectLatest { following ->
                binding.txtCounterFollowing.text = following.count().toString()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.selectedUserPosts.collectLatest { posts ->
                Timber.d(posts.toString())
                binding.txtCounterPosts.text = posts.count().toString()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.isSelectedUserFollowedByLoggedUser.collectLatest { isFollowed ->
                if (isFollowed) // isFollowed
                {
                    binding.butFollow.text = getString(R.string.unfollow)
                }
                else // is not followed
                {
                    binding.butFollow.text = getString(R.string.follow)
                }
            }
        }
    }

    private fun setupClickListeners()
    {
        with(binding)
        {
            butFollow.setOnClickListener {
                viewModel.followUnfollow()
            }
        }
    }
}