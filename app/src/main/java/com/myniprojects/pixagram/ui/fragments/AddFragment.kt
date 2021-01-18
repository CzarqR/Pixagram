package com.myniprojects.pixagram.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.imageadapter.ImageAdapter
import com.myniprojects.pixagram.databinding.FragmentAddBinding
import com.myniprojects.pixagram.utils.*
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

    private lateinit var uri: Uri

    private var currentImageUri: Uri? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSaved ->
        if (isSaved)
        {
            Timber.d(uri.toString())
            viewModel.captureImage(uri)
        }
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
        setupClickListeners()
    }


    private fun setupClickListeners()
    {
        binding.butMakeNewImage.setOnClickListener {

            val photoFile = File.createTempFile(
                "IMG_",
                ".jpg",
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )

            uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )

            takePicture.launch(uri)
        }

        binding.butPost.setOnClickListener {
            currentImageUri?.let {
                viewModel.postImage(it, binding.edTxtDesc.input)
            }
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
                currentImageUri = it
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

        lifecycleScope.launchWhenStarted {
            viewModel.isUploading.collectLatest {
                Timber.d("LoadingState collected $it")
                setLoadingState(it)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.uploadingMsg.collectLatest { event ->
                Timber.d("Event retrieved $event")
                event?.getContentIfNotHandled()?.let { msg ->

                    binding.host.showSnackbar(
                        message = msg,
                        length = Snackbar.LENGTH_SHORT,
                        buttonText = getString(R.string.ok)
                    )

                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean)
    {
        with(binding)
        {
            progressBarUpload.isVisible = isLoading
            mainBody.alpha = if (isLoading) 0.5f else 1f
            mainBody.setViewAndChildrenEnabled(!isLoading)
        }
    }
}