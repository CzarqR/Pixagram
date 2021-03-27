package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.myniprojects.pixagram.utils.ext.*
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
class ProfileFragment : Fragment(R.layout.fragment_user)
{
    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var postAdapter: PostAdapter

    private val viewModel: UserViewModel by viewModels()
    private val binding by viewBinding(FragmentUserBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initWithLoggedUser()

        setupView()
        setupCollecting()
        setupRecycler()
        setupClickListener()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_toolbar_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            R.id.miSettings ->
            {
                Timber.d("Settings selected")
                findNavController().navigate(R.id.settingsFragment)
                true
            }
            R.id.miSignOut ->
            {
                viewModel.signOut()
                true
            }
            R.id.miEdit ->
            {
                Timber.d("Edit profile clicked")
                findNavController().navigate(R.id.editProfileFragment)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setupView()
    {
        binding.buttonsArea.isVisible = false
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
                    setActionBarTitle(getString(R.string.your_profile_format, it.username))
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
    }

    private fun setupRecycler()
    {
        postAdapter.postClickListener = PostClickListener(
            commentClick = ::commentClick,
            imageClick = ::imageClick,
            linkClick = ::linkClick,
            mentionClick = ::mentionClick,
            tagClick = ::tagClick,
            likeClick = ::likePost,
            menuReportClick = ::menuReportClick,
            shareClick = ::shareClick
        )

        binding.rvPosts.adapter = postAdapter

        /**
         * Collect selected user posts
         */
        lifecycleScope.launchWhenStarted {
            viewModel.userPosts.collectLatest { postsStatus ->

                when (postsStatus)
                {
                    DataStatus.Loading ->
                    {
                        binding.rvPosts.isVisible = false
                        binding.linLayEmptyData.isVisible = false
                    }
                    is DataStatus.Success ->
                    {
                        binding.txtCounterPosts.text = postsStatus.data.count().toString()
                        val data = postsStatus.data.toList().sortedByDescending {
                            it.second.time
                        }
                        postAdapter.submitList(data)

                        setCounterStatus(data.isEmpty())
                    }
                    is DataStatus.Failed ->
                    {
                    }
                }.exhaustive
            }
        }
    }

    private fun setCounterStatus(isListEmpty: Boolean)
    {
        binding.rvPosts.isVisible = !isListEmpty
        binding.linLayEmptyData.isVisible = isListEmpty
    }

    private fun setupClickListener()
    {
        binding.imgAvatar.setOnClickListener {

            viewModel.selectedUser.value?.imageUrl?.let { avatarUrl ->
                val action = ProfileFragmentDirections.actionProfileFragmentToDetailAvatarFragment(
                    avatarUrl = avatarUrl
                )
                findNavController().navigate(action)
            }
        }
    }

    // region post callbacks


    private fun commentClick(postId: String)
    {
        val action = ProfileFragmentDirections.actionProfileFragmentToCommentFragment(
            postId = postId
        )
        findNavController().navigate(action)
    }

    private fun imageClick(post: PostWithId)
    {
        val action = ProfileFragmentDirections.actionProfileFragmentToDetailPostFragment(
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
        if (viewModel.isOwnAccountUsername(mention)) // user  clicked on own profile
        {
            binding.userLayout.showSnackbarGravity(
                message = getString(R.string.you_are_currenly_on_your_profile)
            )
        }
        else
        {
            val action = ProfileFragmentDirections.actionProfileFragmentToUserFragment(
                user = User(username = mention),
                loadUserFromDb = true
            )
            findNavController().navigate(action)
        }
    }

    private fun tagClick(tag: String)
    {
        Timber.d("Tag clicked $tag")
        val action = ProfileFragmentDirections.actionProfileFragmentToTagFragment(
            tag = Tag(tag, -1),
        )
        findNavController().navigate(action)
    }

    private fun likePost(postId: String, status: Boolean)
    {
        viewModel.setLikeStatus(postId, status)
    }

    private fun menuReportClick(postId: String)
    {
        Timber.d("Report click for post $postId")
        showToastNotImpl()
    }

    private fun shareClick(postId: String)
    {
        Timber.d("Share click for post $postId")
        showToastNotImpl()
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