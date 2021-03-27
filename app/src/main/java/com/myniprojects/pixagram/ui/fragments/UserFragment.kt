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
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.databinding.FragmentUserBinding
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.ui.MainActivity
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.ext.setActionBarTitle
import com.myniprojects.pixagram.utils.ext.showSnackbarGravity
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.DataStatus
import com.myniprojects.pixagram.utils.status.SearchFollowStatus
import com.myniprojects.pixagram.vm.IsUserFollowed
import com.myniprojects.pixagram.vm.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.util.*
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
            commentClick = ::commentCLick,
            imageClick = ::imageClick,
            linkClick = ::linkClick,
            mentionClick = ::mentionClick,
            tagClick = ::tagClick,
            likeClick = ::likePost
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

    // region post callbacks

    private fun commentCLick(postId: String)
    {
        val action = UserFragmentDirections.actionUserFragmentToCommentFragment(
            postId = postId
        )
        findNavController().navigate(action)
    }

    private fun imageClick(post: PostWithId)
    {
        val action = UserFragmentDirections.actionUserFragmentToDetailPostFragment(
            post = post.second,
            postId = post.first
        )
        findNavController().navigate(action)
    }

    private fun linkClick(link: String)
    {
        Timber.d("Link clicked $link")
        (activity as MainActivity).tryOpenUrl(link) {
            binding.userLayout.showSnackbarGravity(
                message = getString(R.string.could_not_open_browser)
            )
        }
    }

    private fun mentionClick(mention: String)
    {
        if (viewModel.isOwnAccountUsername(mention)) // user clicked on own profile
        {
            findNavController().navigate(R.id.profileFragment)
        }
        else
        {
            // check if mention is not the same as current user
            if (viewModel.selectedUser.value?.usernameComparator != mention.toLowerCase(Locale.ENGLISH))
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
                    message = getString(R.string.you_are_currenly_on_this_profile)
                )
            }

        }
    }

    private fun tagClick(tag: String)
    {
        Timber.d("Tag clicked $tag")
        val action = UserFragmentDirections.actionUserFragmentToTagFragment(
            tag = Tag(tag, -1),
        )
        findNavController().navigate(action)
    }

    private fun likePost(postId: String, status: Boolean)
    {
        viewModel.setLikeStatus(postId, status)
    }

    // endregion

    /**
     * When View is destroyed adapter should cancel scope in every ViewHolder
     */
    override fun onDestroyView()
    {
        super.onDestroyView()
        postAdapter.cancelScopes()
    }
}