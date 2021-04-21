package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.ui.fragments.utils.AbstractUserFragment
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.ext.normalize
import com.myniprojects.pixagram.utils.ext.showSnackbarGravity
import com.myniprojects.pixagram.utils.ext.showToastNotImpl
import com.myniprojects.pixagram.vm.IsUserFollowed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class UserFragment : AbstractUserFragment()
{
    private val args: UserFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInitialized.value)
        {
            if (args.loadUserFromDb)
                if (args.user.username.isNotEmpty())
                    viewModel.initWithUsername(args.user.username)
                else
                    viewModel.initWithUserId(args.user.id)
            else
                viewModel.initUser(args.user)
        }

        setupBaseCollecting()
        setupCollecting()
        setupClickListeners()
        initRecyclers(false)
    }

    private fun setupCollecting()
    {
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

            butMessage.setOnClickListener {
                showToastNotImpl()
            }
        }
    }

    // region post callbacks

    override fun profileClick(postOwner: String)
    {
    }

    override fun commentClick(postId: String)
    {
        val action = UserFragmentDirections.actionUserFragmentToCommentFragment(
            postId = postId
        )
        findNavController().navigate(action)
    }

    override fun imageClick(postWithId: PostWithId)
    {
        val action = UserFragmentDirections.actionUserFragmentToDetailPostFragment(
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
            // check if mention is not the same as current user
            if (viewModel.selectedUser.value?.usernameComparator != mention.normalize())
            {
                val action = UserFragmentDirections.actionUserFragmentSelf(
                    user = User(username = mention),
                    loadUserFromDb = true
                )
                findNavController().navigate(action)
            }
            else
            {
                Timber.d("User clicked on mention with the same profile")
                binding.userLayout.showSnackbarGravity(
                    message = getString(R.string.you_are_currently_on_this_profile)
                )
            }

        }
    }

    override fun tagClick(tag: String)
    {
        Timber.d("Tag clicked $tag")
        val action = UserFragmentDirections.actionUserFragmentToTagFragment(
            tag = Tag(tag, -1),
        )
        findNavController().navigate(action)
    }

    // endregion
}