package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.imageadapter.ImageAdapter
import com.myniprojects.pixagram.databinding.FragmentAddBinding
import com.myniprojects.pixagram.utils.Constants
import com.myniprojects.pixagram.utils.viewBinding
import com.myniprojects.pixagram.vm.AddViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class AddFragment : Fragment(R.layout.fragment_add)
{
    @Inject
    lateinit var imageAdapter: ImageAdapter

    @Inject
    lateinit var glide: RequestManager

    private val binding by viewBinding(FragmentAddBinding::bind)
    private val viewModel: AddViewModel by activityViewModels()

    private var state: State = State.SELECTION
        set(value)
        {
            field = value
            setVisibility(value == State.SELECTED)
        }

    private fun setVisibility(visibility: Boolean)
    {
        with(binding)
        {
            butCancel.isVisible = visibility
            imgSelected.isVisible = visibility
            butPost.isVisible = visibility
            txtFieldDesc.isVisible = visibility


            (binding.rvGallery.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (visibility) "1:1" else ""

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        setupRecycler()

        viewModel.loadAllImages()
        setupCollecting()
    }

    private fun setupRecycler()
    {
        with(binding.rvGallery)
        {
            layoutManager = GridLayoutManager(requireContext(), Constants.GALLERY_COLUMNS)
            adapter = imageAdapter.apply {
                clickListener = viewModel::selectImage
            }
        }
    }


    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.allImagesFromGallery.collectLatest {
                if (it.isNotEmpty())
                {
                    imageAdapter.submitList(it)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.selectedImage.collectLatest {
                state = if (it == null)
                {
                    State.SELECTION
                }
                else
                {
                    glide
                        .load(it)
                        .into(binding.imgSelected)
                    State.SELECTED
                }
            }
        }
    }

    private enum class State
    {
        SELECTION,
        SELECTED
    }
}