package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.material.button.MaterialButton
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentDetailPostBinding
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.vm.DetailPostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class DetailPostFragment : Fragment(R.layout.fragment_detail_post)
{
    @Inject
    lateinit var imageLoader: ImageLoader

    private val viewModel: DetailPostViewModel by viewModels()

    private val binding by viewBinding(FragmentDetailPostBinding::bind)
    private val args: DetailPostFragmentArgs by navArgs()

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

        viewModel.initPost(args.postId)
        setupCollecting()
        setClickListeners()

        loadImage()
    }


    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.postStatus.collectLatest {
                when (it)
                {
                    is GetStatus.Failed ->
                    {

                    }
                    GetStatus.Loading ->
                    {

                    }
                    is GetStatus.Success ->
                    {
                        isPostLiked = it.data.isPostLikeByLoggedUser
                    }
                }.exhaustive
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


    private fun loadImage()
    {
        val request = ImageRequest.Builder(requireContext())
            .data(args.post.imageUrl)
            .target { drawable ->
                binding.imgPost.setImageDrawable(drawable)
            }
            .build()
        imageLoader.enqueue(request)

        binding.txtUsername.text = "TODO load username"


    }

    private fun setClickListeners()
    {
        binding.butComment.setOnClickListener {
            val action = DetailPostFragmentDirections.actionDetailPostFragmentToCommentFragment(
                postId = args.postId
            )
            findNavController().navigate(action)
        }

        binding.butBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun share()
    {
        Timber.d("Share")
    }


    private fun like()
    {
        Timber.d("Like")
    }


}