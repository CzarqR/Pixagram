package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import coil.request.ImageRequest
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.adapters.postadapter.PostClickListener
import com.myniprojects.pixagram.databinding.FragmentUserBinding
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.ext.setActionBarTitle
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.DataStatus
import com.myniprojects.pixagram.utils.status.SearchFollowStatus
import com.myniprojects.pixagram.vm.IsUserFollowed
import com.myniprojects.pixagram.vm.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class UserFragment : Fragment(R.layout.fragment_user)
{
    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var postAdapter: PostAdapter

    private val binding by viewBinding(FragmentUserBinding::bind)
    private val viewModel: UserViewModel by viewModels()

    private val args: UserFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        if (args.loadUserFromDb)
            if (args.user.username.isNotEmpty())
                viewModel.initWithUsername(args.user.username)
            else
                viewModel.initWithUserId(args.user.id)
        else
            viewModel.initUser(args.user)

        setupCollecting()
        setupRecycler()
        setupClickListeners()
    }

    private fun setupCollecting()
    {
        /**
         * Collect user data
         */
        lifecycleScope.launchWhenStarted {
            viewModel.selectedUser.collectLatest {
                if (it != null)
                {
                    with(binding)
                    {
                        txtDesc.text = it.bio
                        txtFullName.text = it.fullName

                        val request = ImageRequest.Builder(requireContext())
                            .data(it.imageUrl)
                            .target { drawable ->
                                imgAvatar.setImageDrawable(drawable)
                            }
                            .build()

                        imageLoader.enqueue(request)
                    }
                    setActionBarTitle(it.username)
                }
            }
        }

        /**
         * Collect followers of selected user
         */
        lifecycleScope.launchWhenStarted {
            viewModel.userFollowersFlow.collectLatest { status ->

                when (status)
                {
                    SearchFollowStatus.Loading, SearchFollowStatus.Sleep ->
                    {
                        Timber.d("Loading followers from db")
                    }
                    is SearchFollowStatus.Success ->
                    {
                        binding.txtCounterFollowers.text = status.result.size.toString()
                    }
                }.exhaustive
            }
        }

        /**
         * Collect selected user following users
         */
        lifecycleScope.launchWhenStarted {
            viewModel.userFollowingFlow.collectLatest { status ->
                when (status)
                {
                    SearchFollowStatus.Loading, SearchFollowStatus.Sleep ->
                    {
                        Timber.d("Loading following from db")
                    }
                    is SearchFollowStatus.Success ->
                    {
                        binding.txtCounterFollowing.text = status.result.size.toString()
                    }
                }.exhaustive
            }
        }

        /**
         * Collect data which tells if selected user
         * is already followed by logged user
         */
        lifecycleScope.launchWhenStarted {
            viewModel.isSelectedUserFollowedByLoggedUser.collectLatest { isFollowedStatus ->
                when (isFollowedStatus)
                {
                    IsUserFollowed.UNKNOWN ->
                    {
                        /**
                         * When fragment is opened follow button will be disabled
                         * Displayed text: [R.string.follow]
                         */
                        binding.butFollow.text = getString(R.string.follow)
                        binding.butFollow.isEnabled = false
                    }
                    IsUserFollowed.YES ->
                    {
                        binding.butFollow.text = getString(R.string.unfollow)
                        binding.butFollow.isEnabled = true
                    }
                    IsUserFollowed.NO ->
                    {
                        binding.butFollow.text = getString(R.string.follow)
                        binding.butFollow.isEnabled = true
                    }
                }.exhaustive
            }
        }

        /**
         * Collect state if following/unfollowing
         * operation is in progress
         */
        lifecycleScope.launchWhenStarted {
            viewModel.canDoFollowUnfollowOperation.collectLatest { canBeClicked ->
                binding.butFollow.isEnabled = canBeClicked
            }
        }

        /**
         * Collect state that tells if user has not been found
         */
        lifecycleScope.launchWhenStarted {
            viewModel.userNotFound.collectLatest { userNotFound ->
                binding.txtUserNotFound.text = getString(
                    R.string.user_not_found_format,
                    args.user.username
                )
                binding.linLayUserNotFound.isVisible = userNotFound
                binding.userLayout.isVisible = !userNotFound
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

            imgAvatar.setOnClickListener {

                viewModel.selectedUser.value?.imageUrl?.let { avatarUrl ->
                    val action = UserFragmentDirections.actionUserFragmentToDetailAvatarFragment(
                        avatarUrl = avatarUrl
                    )
                    findNavController().navigate(action)
                }
            }
        }
    }

    /**
     * Todo display properly: loading / error / empty list
     */
    private fun setupRecycler()
    {

        postAdapter.postClickListener = PostClickListener(
            commentListener = ::commentCLick
        )



        binding.rvPosts.adapter = postAdapter

        /**
         * Collect selected user posts
         */
        lifecycleScope.launchWhenStarted {
            viewModel.userPosts.collectLatest { postsStatus ->
                Timber.d("POST FR $postsStatus")
                when (postsStatus)
                {
                    DataStatus.Loading ->
                    {
                    }
                    is DataStatus.Success ->
                    {
                        val c = postsStatus.data.count()
                        binding.txtCounterPosts.text = c.toString()
                        postAdapter.submitList(postsStatus.data.toList())

                        binding.rvPosts.isVisible = c > 0
                        binding.linLayEmptyData.isVisible = c <= 0
                    }
                    is DataStatus.Failed ->
                    {
                    }
                }.exhaustive
            }
        }
    }


    private fun commentCLick(postId: String)
    {
        val action = UserFragmentDirections.actionUserFragmentToCommentFragment(
            postId = postId
        )
        findNavController().navigate(action)
    }
}