package com.myniprojects.pixagram.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.material.button.MaterialButton
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.databinding.FragmentDetailPostBinding
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.ui.fragments.utils.AbstractFragmentPost
import com.myniprojects.pixagram.utils.ext.*
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.vm.DetailPostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class DetailPostFragment : AbstractFragmentPost(R.layout.fragment_detail_post)
{
    @Inject
    lateinit var imageLoader: ImageLoader

    override val viewModel: DetailPostViewModel by viewModels()

    override val binding by viewBinding(FragmentDetailPostBinding::bind)
    private val args: DetailPostFragmentArgs by navArgs()

    private lateinit var post: PostWithId

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        post = args.postId to args.post

        viewModel.initPost(post)
        setupCollecting()
        setClickListeners()
        setInfoViews()

        loadImage()
    }

    private fun setInfoViews()
    {
        with(binding)
        {
            txtDesc.text = post.second.desc
        }
    }


    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.likeStatus.collectLatest {
                when (it)
                {
                    is GetStatus.Failed ->
                    {

                    }
                    GetStatus.Loading ->
                    {
                        binding.txtLikesCounter.text = getString(R.string.loading_dots)
                    }
                    is GetStatus.Success ->
                    {
                        isPostLiked = it.data.isPostLikeByLoggedUser
                        binding.txtLikesCounter.text = it.data.likeCounter.formatWithSpaces()
                    }
                }.exhaustive
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.userStatus.collectLatest {
                when (it)
                {
                    is GetStatus.Failed ->
                    {

                    }
                    GetStatus.Loading ->
                    {
                        binding.txtUsername.text = getString(R.string.loading_dots)
                    }
                    is GetStatus.Success ->
                    {
                        binding.txtUsername.text = it.data.username
                    }
                }.exhaustive
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.commentStatus.collectLatest {
                when (it)
                {
                    is GetStatus.Failed ->
                    {

                    }
                    GetStatus.Loading ->
                    {
                        binding.txtComments.text = getString(
                            R.string.comments_format,
                            getString(R.string.loading_dots)
                        )
                    }
                    is GetStatus.Success ->
                    {
                        binding.txtComments.text = binding.context.getString(
                            R.string.comments_format,
                            it.data.formatWithSpaces()
                        )
                    }
                }.exhaustive
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.isInfoShown.collectLatest {
                setInfoViewsVisibility(it)
            }
        }

    }


    private var isPostLiked = false
        set(value)
        {
            field = value

            if (value) // Post is liked
            {
                with(binding)
                {
                    (butLike as MaterialButton).apply {
                        icon = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_baseline_favorite_24
                        )
                        setIconTintResource(R.color.red_on_surface)
                    }
                }
            }
            else // post is not liked
            {
                with(binding)
                {
                    (butLike as MaterialButton).apply {
                        icon = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_outline_favorite_border_24
                        )
                        setIconTintResource(R.color.button_on_surface)
                    }

                }
            }
        }


    private fun setInfoViewsVisibility(isVisible: Boolean)
    {
        with(binding)
        {
            txtDesc.isVisible = isVisible
            imgLikedCounter.isVisible = isVisible
            txtLikesCounter.isVisible = isVisible
            txtComments.isVisible = isVisible


            /**
             * change icon of show button
             */
            ContextCompat.getDrawable(
                requireContext(),
                if (isVisible) R.drawable.ic_arrow_down_24 else R.drawable.ic_arrow_up_24
            )?.let {
                (binding.butShow as MaterialButton).icon = it
            }

            val bc = if (isVisible) ContextCompat.getColor(
                requireContext(),
                R.color.font_background
            )
            else
                Color.TRANSPARENT

            binding.topBar.setBackgroundColor(bc)
            binding.bottomBar.setBackgroundColor(bc)
        }
    }

    private fun loadImage()
    {
        val request = ImageRequest.Builder(requireContext())
            .data(post.second.imageUrl)
            .target { drawable ->
                binding.imgPost.setImageDrawable(drawable)
            }
            .build()
        imageLoader.enqueue(request)
    }

    private fun setClickListeners()
    {
        with(binding)
        {
            butComment.setOnClickListener {
                commentClick(post.first)
            }

            txtComments.setOnClickListener {
                commentClick(post.first)
            }

            butBack.setOnClickListener {
                findNavController().popBackStack()
            }

            butLike.setOnClickListener {
                viewModel.setLikeStatus(post.first, !isPostLiked)
            }

            txtUsername.setOnClickListener {
                profileClick(post.second.owner)
            }

            butShow.setOnClickListener {
                viewModel.changeCollapse()
            }

            txtDesc.setOnHashtagClickListener { _, text -> tagClick(text.toString()) }
            txtDesc.setOnHyperlinkClickListener { _, text -> linkClick(text.toString()) }
            txtDesc.setOnMentionClickListener { _, text -> mentionClick(text.toString()) }

            butOptions.setOnClickListener {
                showPopupMenu(it)
            }

            butShare.setOnClickListener {
                shareClick(post.first)
            }

            linLayLikeCounter.setOnClickListener {
                likeCounterClick(post.first)
            }
        }
    }

    override fun profileClick(postOwner: String)
    {
        if (viewModel.isOwnAccountId(postOwner)) // user clicked on own profile
        {
            findNavController().navigate(R.id.profileFragment)
        }
        else
        {
            val action = DetailPostFragmentDirections.actionDetailPostFragmentToUserFragment(
                user = User(id = postOwner),
                loadUserFromDb = true
            )
            findNavController().navigate(action)
        }
    }

    override fun commentClick(postId: String)
    {
        val action = DetailPostFragmentDirections.actionDetailPostFragmentToCommentFragment(
            postId = postId
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
            val action = DetailPostFragmentDirections.actionDetailPostFragmentToUserFragment(
                user = User(username = mention),
                loadUserFromDb = true
            )
            findNavController().navigate(action)
        }
    }

    override fun tagClick(tag: String)
    {
        Timber.d("Tag clicked $tag")
        val action = DetailPostFragmentDirections.actionDetailPostFragmentToTagFragment(
            tag = Tag(tag, -1),
        )
        findNavController().navigate(action)
    }

    private fun showPopupMenu(view: View)
    {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.menu_post_dropdown_collapse)

        popupMenu.menu.findItem(R.id.mi_collapse).isVisible = false

        popupMenu.setOnMenuItemClickListener { menuItem ->

            return@setOnMenuItemClickListener when (menuItem.itemId)
            {
                R.id.mi_report ->
                {
                    showToastNotImpl()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}