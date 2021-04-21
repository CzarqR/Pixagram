package com.myniprojects.pixagram.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.material.snackbar.Snackbar
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.imageadapter.ImageAdapter
import com.myniprojects.pixagram.databinding.FragmentUploadBinding
import com.myniprojects.pixagram.ui.MainActivity
import com.myniprojects.pixagram.utils.consts.Constants
import com.myniprojects.pixagram.utils.ext.*
import com.myniprojects.pixagram.utils.status.FirebaseStatus
import com.myniprojects.pixagram.vm.UploadViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class UploadFragment : Fragment(R.layout.fragment_upload)
{
    @Inject
    lateinit var imageAdapter: ImageAdapter

    @Inject
    lateinit var imageLoader: ImageLoader

    private val binding by viewBinding(FragmentUploadBinding::bind)
    private val viewModel: UploadViewModel by activityViewModels()

    private lateinit var uri: Uri

    private var currentImageUri: Uri? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSaved ->
        if (isSaved)
        {
            Timber.d(uri.toString())
            viewModel.captureImage(uri)
        }
    }

    private val requestCameraPermissions =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted)
                {
                    takeImageFromCamera()
                }
                else
                {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.CAMERA
                        )
                    )
                    {
                        //never ask again
                        (requireActivity() as MainActivity).showSnackbar(
                            message = getString(R.string.message_camera_never_ask),
                            buttonText = getString(R.string.settings),
                            action = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts(
                                        "package",
                                        requireContext().packageName,
                                        null
                                    )
                                }
                                startActivity(intent)
                            }
                        )
                    }
                }
            }

    private val requestStoragePermissions =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted)
                {
                    viewModel.loadAllImagesFromGallery()
                }
                else
                {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    )
                    {
                        //never ask again
                        (requireActivity() as MainActivity).showSnackbar(
                            message = getString(R.string.message_storage_never_ask),
                            buttonText = getString(R.string.settings),
                            action = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts(
                                        "package",
                                        requireContext().packageName,
                                        null
                                    )
                                }
                                startActivity(intent)
                            }
                        )
                    }
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

        checkReadStoragePermAndLoadImages()
        setupCollecting()
        setupClickListeners()
    }

    private fun setStoragePermissionStatus(isPermissionGranted: Boolean)
    {
        binding.butStoragePermission.isVisible = !isPermissionGranted
        binding.rvGallery.isVisible = isPermissionGranted
        binding.txtAStoragePermission.isVisible = !isPermissionGranted
    }

    private fun checkReadStoragePermAndLoadImages()
    {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PERMISSION_GRANTED
        )
        {
            setStoragePermissionStatus(true)
            viewModel.loadAllImagesFromGallery()
        }
        else
        {
            setStoragePermissionStatus(false)
        }
    }

    private fun takeImageFromCamera()
    {
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

    private fun setupClickListeners()
    {
        binding.butMakeNewImage.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PERMISSION_GRANTED
            )
            {
                takeImageFromCamera()
            }
            else
            {
                requestCameraPermissions.launch(Manifest.permission.CAMERA)
            }
        }

        binding.butStoragePermission.setOnClickListener {
            Timber.d("Click")
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PERMISSION_GRANTED
            )
            {
                viewModel.loadAllImagesFromGallery()
            }
            else
            {
                requestStoragePermissions.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        binding.butPost.setOnClickListener {
            uploadPost()
        }
    }

    private val _uploadStatus: MutableStateFlow<FirebaseStatus> = MutableStateFlow(
        FirebaseStatus.Sleep
    )

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
            layoutManager = GridLayoutManager(
                requireContext(),
                Constants.GALLERY_COLUMNS
            )
            adapter = imageAdapter.apply {
                clickListener = viewModel::selectImageFromGallery
            }
        }
    }


    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            _uploadStatus.collectLatest {
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
                        binding.host.showSnackbarGravity(
                            message = it.message.getFormattedMessage(requireContext()),
                            length = Snackbar.LENGTH_SHORT,
                            buttonText = getString(R.string.ok)
                        )
                        hideKeyboard()
                    }
                    is FirebaseStatus.Success ->
                    {
                        setLoadingState(false)
                        binding.host.showSnackbarGravity(
                            message = it.message.getFormattedMessage(requireContext()),
                            length = Snackbar.LENGTH_SHORT,
                            buttonText = getString(R.string.see),
                            action = {
                                /**
                                 * would be better to navigate to DetailsFragment
                                 * but it requires more work and navigating to profile
                                 * looks fine because recently added post is always on top
                                 */
                                findNavController().navigate(R.id.profileFragment)
                            }
                        )

                        viewModel.unSelectImage()
                        binding.edTxtDesc.setText("")
                        hideKeyboard()
                    }
                }.exhaustive
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.allImagesFromGallery.collectLatest {
                if (it.isNotEmpty())
                {
                    setStoragePermissionStatus(true)
                    binding.txtEmptyResult.isVisible = false
                    imageAdapter.submitList(it)
                }
                else // show empty state
                {
                    binding.txtEmptyResult.isVisible = true
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