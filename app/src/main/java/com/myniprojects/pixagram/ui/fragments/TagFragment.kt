package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.databinding.FragmentTagBinding
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.ui.fragments.utils.AbstractFragmentStateRecycler
import com.myniprojects.pixagram.utils.ext.*
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.vm.TagViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class TagFragment : AbstractFragmentStateRecycler(
    R.layout.fragment_tag,
    StateData(
        emptyStateIcon = R.drawable.ic_outline_dynamic_feed_24,
        emptyStateText = R.string.nothing_to_show_home,
        bottomRecyclerPadding = R.dimen.bottom_place_holder_user
    )
)
{
    @Inject
    lateinit var glide: RequestManager

    override val binding by viewBinding(FragmentTagBinding::bind)
    override val viewModel: TagViewModel by activityViewModels()

    private val args: TagFragmentArgs by navArgs()

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initTag(args.tag)
        setActionBarTitle(getString(R.string.tag_title_format, args.tag.title))
        setupCollecting()

    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.tag.collectLatest {

                when (it)
                {
                    GetStatus.Sleep -> Unit
                    is GetStatus.Failed ->
                    {
                    }
                    GetStatus.Loading ->
                    {
                        with(binding)
                        {
                            txtPosts.text = getString(R.string.loading_posts_counter)
                        }
                    }
                    is GetStatus.Success ->
                    {
                        with(binding)
                        {
                            txtPosts.text = resources.getQuantityString(
                                R.plurals.tag_counter_post,
                                it.data.count.toInt(),
                                it.data.count
                            )
                        }
                    }
                }.exhaustive


            }
        }

        /**
         * Collect posts to set image in AppBar
         */
        lifecycleScope.launchWhenStarted {
            viewModel.postToDisplay.collectLatest { postsStatus ->
                if (postsStatus is GetStatus.Success)
                {
                    glide
                        .load(postsStatus.data.maxByOrNull { it.second.time }?.second?.imageUrl)
                        .into(binding.imgTag)
                }
            }
        }
    }

    // region post callbacks

    override fun profileClick(postOwner: String)
    {
        if (viewModel.isOwnAccountId(postOwner)) // user clicked on own profile (currently impossible because there are no own post on home feed)
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

    override fun commentClick(postId: String)
    {
        val action = TagFragmentDirections.actionTagFragmentToCommentFragment(
            postId = postId
        )
        findNavController().navigate(action)
    }

    override fun imageClick(postWithId: PostWithId)
    {
        val action = TagFragmentDirections.actionTagFragmentToDetailPostFragment(
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
            val action = TagFragmentDirections.actionTagFragmentToUserFragment(
                user = User(username = mention),
                loadUserFromDb = true
            )
            findNavController().navigate(action)
        }
    }

    override fun tagClick(tag: String)
    {
        if (tag isEqualTo (viewModel.tag.value as? GetStatus.Success<Tag>)?.data?.title) // same tag was clicked
        {
            showSnackbar(R.string.you_are_currently_on_this_tag)
        }
        else
        {
            val action = TagFragmentDirections.actionTagFragmentSelf(
                tag = Tag(tag, -1),
            )
            findNavController().navigate(action)
        }
    }

    // endregion

}