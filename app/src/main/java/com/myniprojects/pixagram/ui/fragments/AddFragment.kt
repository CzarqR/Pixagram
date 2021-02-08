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
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.material.snackbar.Snackbar
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.imageadapter.ImageAdapter
import com.myniprojects.pixagram.databinding.FragmentAddBinding
import com.myniprojects.pixagram.utils.consts.Constants
import com.myniprojects.pixagram.utils.ext.*
import com.myniprojects.pixagram.utils.status.FirebaseStatus
import com.myniprojects.pixagram.vm.AddViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AddFragment : Fragment(R.layout.fragment_add)
{
    @Inject
    lateinit var imageAdapter: ImageAdapter

    @Inject
    lateinit var imageLoader: ImageLoader

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

        viewModel.loadAllImagesFromGallery()
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
            uploadPost()
        }
    }

    private val _uploadStatus: MutableStateFlow<FirebaseStatus> = MutableStateFlow(FirebaseStatus.Sleep)
    private val uploadStatus: StateFlow<FirebaseStatus> = _uploadStatus.asStateFlow()

    private fun uploadPost()
    {
        /**
         * If status is equal to [FirebaseStatus.Loading]
         * do not upload new post
         */
        if (_uploadStatus.value != FirebaseStatus.Loading)
        {
            lifecycleScope.launch {
                currentImageUri?.let { uri ->
                    viewModel.postImage(
                        uri = uri,
                        desc = binding.edTxtDesc.input,
                        hashtags = binding.edTxtDesc.hashtags,
                        mentions = binding.edTxtDesc.mentions
                    ).collectLatest { uploadStatus ->
                        _uploadStatus.value = uploadStatus
                    }
                }
            }
        }
        else
        {
            Timber.d("Skipping upload. Previous request is in progress")
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
            uploadStatus.collectLatest {
                when (it)
                {
                    FirebaseStatus.Sleep -> Unit// do nothing
                    FirebaseStatus.Loading ->
                    {
                        setLoadingState(true)
                    }
                    is FirebaseStatus.Failed ->
                    {
                        setLoadingState(false)
                        binding.host.showSnackbar(
                            message = it.message.getFormattedMessage(requireContext()),
                            length = Snackbar.LENGTH_SHORT,
                            buttonText = getString(R.string.ok)
                        )
                    }
                    is FirebaseStatus.Success ->
                    {
                        setLoadingState(false)
                        binding.host.showSnackbar(
                            message = it.message.getFormattedMessage(requireContext()),
                            length = Snackbar.LENGTH_SHORT,
                            buttonText = getString(R.string.ok)
                        )
                    }
                }.exhaustive
            }
        }

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

                    val request = ImageRequest.Builder(requireContext())
                        .data(it)
                        .target { drawable ->
                            binding.imgSelected.setImageDrawable(drawable)
                        }
                        .build()

                    imageLoader.enqueue(request)
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