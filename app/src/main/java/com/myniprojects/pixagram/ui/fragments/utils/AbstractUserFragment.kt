package com.myniprojects.pixagram.ui.fragments.utils

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.MarginPageTransformer
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.material.tabs.TabLayoutMediator
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.adapters.postcategoryadapter.PostCategoryAdapter
import com.myniprojects.pixagram.adapters.postcategoryadapter.StateRecyclerData
import com.myniprojects.pixagram.databinding.FragmentUserBinding
import com.myniprojects.pixagram.utils.ext.*
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.utils.status.SearchFollowStatus
import com.myniprojects.pixagram.vm.DisplayPostCategory
import com.myniprojects.pixagram.vm.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
abstract class AbstractUserFragment : AbstractFragmentPost(R.layout.fragment_user)
{
    @Inject
    lateinit var uploadAdapter: PostAdapter

    @Inject
    lateinit var mentionedAdapter: PostAdapter

    @Inject
    lateinit var likedAdapter: PostAdapter

    @Inject
    lateinit var imageLoader: ImageLoader
    override val binding by viewBinding(FragmentUserBinding::bind)
    override val viewModel: UserViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.linLayFollowers.setOnClickListener { openFollowers() }
        binding.linLayFollowing.setOnClickListener { openFollowing() }
    }

    private fun openFollowers()
    {
        openDialogWithListOfUsers(
            statusFlow = viewModel.getFollowers(),
            title = R.string.followers,
            emptyText = R.string.user_have_no_following,
            errorText = R.string.something_went_wrong
        )
    }

    private fun openFollowing()
    {
        openDialogWithListOfUsers(
            statusFlow = viewModel.getFollowing(),
            title = R.string.following,
            emptyText = R.string.user_have_no_followers,
            errorText = R.string.something_went_wrong
        )
    }


    protected fun setupBaseCollecting()
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
         * Collect number of posts
         */
        lifecycleScope.launchWhenStarted {
            viewModel.uploadedPosts.collectLatest {
                if (it is GetStatus.Success)
                {
                    binding.txtCounterPosts.text = it.data.size.toString()
                }
            }
        }

        /**
         * Collect selected category
         */
        lifecycleScope.launchWhenStarted {
            viewModel.category.collectLatest { selected ->
                binding.tabsPostType.getTabAt(
                    categories.filterValues {
                        it == selected
                    }.keys.elementAt(0)
                )?.select()

            }
        }
    }

    protected fun initRecyclers(
        isProfileFragment: Boolean
    )
    {
        binding.vpRecyclers.setPageTransformer(MarginPageTransformer(8.px))

        uploadAdapter.postClickListener = this
        mentionedAdapter.postClickListener = this
        likedAdapter.postClickListener = this

        val uploads = StateRecyclerData(
            viewModel.uploadedPosts,
            uploadAdapter,
            StateData(
                emptyStateIcon = R.drawable.ic_outline_dynamic_feed_24,
                emptyStateText = if (isProfileFragment) R.string.no_uploads_profile else R.string.no_uploads_user,
                bottomRecyclerPadding = R.dimen.bottom_place_holder_user
            )
        )

        val mentioned = StateRecyclerData(
            viewModel.mentionPosts,
            mentionedAdapter,
            StateData(
                emptyStateIcon = R.drawable.ic_outline_alternate_email_24,
                emptyStateText = if (isProfileFragment) R.string.no_mentions_profile else R.string.no_mentions_user,
                bottomRecyclerPadding = R.dimen.bottom_place_holder_user
            )
        )

        val liked = StateRecyclerData(
            viewModel.likedPosts,
            likedAdapter,
            StateData(
                emptyStateIcon = R.drawable.ic_outline_favorite_border_24,
                emptyStateText = if (isProfileFragment) R.string.no_likes_profile else R.string.no_liked_user,
                bottomRecyclerPadding = R.dimen.bottom_place_holder_user
            )
        )

        val recyclers = listOf(uploads, mentioned, liked)

        val adapter = PostCategoryAdapter(recyclers)

        binding.vpRecyclers.adapter = adapter

        val names = DisplayPostCategory.values().map {
            it.categoryName
        }
        TabLayoutMediator(binding.tabsPostType, binding.vpRecyclers) { tab, pos ->
            tab.text = getString(names[pos])
        }.attach()
    }

    private val categories = hashMapOf(
        0 to DisplayPostCategory.UPLOADED,
        1 to DisplayPostCategory.MENTIONS,
        2 to DisplayPostCategory.LIKED
    )
}