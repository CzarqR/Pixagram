package com.myniprojects.pixagram.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider.getUriForFile
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
import timber.log.Timber
import java.io.File
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

    private lateinit var imagePath: File
    private lateinit var newFile: File
    private lateinit var uri: Uri

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        setupRecycler()

        viewModel.loadAllImages()
        setupCollecting()
        setupClickListeners()


        imagePath = File(requireContext().filesDir, "images")
        newFile = File(imagePath, "default_image.jpg")
        uri = getUriForFile(
            requireContext(),
            requireContext().applicationContext.packageName,
            newFile
        )
    }


    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSaved ->
        if (isSaved)
        {
            Timber.d("Uri: $uri")
            viewModel.captureImage(uri)
        }
    }


    private fun setupClickListeners()
    {
        binding.butMakeNewImage.setOnClickListener {
            takePicture.launch(uri)
        }
    }


    private fun setupRecycler()
    {
        with(binding.rvGallery)
        {
            layoutManager = GridLayoutManager(requireContext(), Constants.GALLERY_COLUMNS)
            adapter = imageAdapter.apply {
                clickListener = viewModel::selectImageFromGallery
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
            viewModel.previewImage.collectLatest {
                if (it == null)
                {
                    setVisibility(false)
                    binding.imgSelected.setImageDrawable(null)
                }
                else
                {
                    setVisibility(true)
                    glide
                        .load(it)
                        .into(binding.imgSelected)
                }
            }
        }
    }
}