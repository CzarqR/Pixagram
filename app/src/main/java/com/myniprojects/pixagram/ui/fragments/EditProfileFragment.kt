package com.myniprojects.pixagram.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentEditProfileBinding
import com.myniprojects.pixagram.utils.ext.*
import com.myniprojects.pixagram.utils.status.EventMessageStatus
import com.myniprojects.pixagram.vm.EditProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile)
{
    @Inject
    lateinit var imageLoader: ImageLoader

    val viewModel: EditProfileViewModel by viewModels()
    private val binding by viewBinding(FragmentEditProfileBinding::bind)

    private lateinit var uri: Uri

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSaved ->
        if (isSaved)
        {
            Timber.d(uri.toString())
            viewModel.setImage(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupCollecting()
        setupView()
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.isAnythingChanged.collectLatest {
                with(binding)
                {
                    butCancel.isVisible = it
                    butSave.isVisible = it
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.baseUser.collectLatest {
                it?.let { user ->
                    val request = ImageRequest.Builder(requireContext())
                        .data(user.imageUrl)
                        .target { drawable ->
                            binding.imgAvatar.setImageDrawable(drawable)
                        }
                        .build()

                    imageLoader.enqueue(request)
                }

            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.newUserData.collectLatest {
                binding.edTxtBio.setTextIfDifferent(it.bio)
                binding.edTxtFullname.setTextIfDifferent(it.fullName)

                if (viewModel.newImageUri.value == null)
                {
                    val request = ImageRequest.Builder(requireContext())
                        .data(it.imageUrl)
                        .target { drawable ->
                            binding.imgAvatar.setImageDrawable(drawable)
                        }
                        .build()

                    imageLoader.enqueue(request)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.editStatus.collectLatest {
                when (it)
                {
                    EventMessageStatus.Sleep ->
                    {
                        setLoadingState(false)
                    }
                    EventMessageStatus.Loading ->
                    {
                        setLoadingState(true)
                    }
                    is EventMessageStatus.Success ->
                    {
                        setLoadingState(false)
                        it.eventMessage.getContentIfNotHandled()?.let { message ->
                            binding.host.showSnackbarGravity(
                                message = message.getFormattedMessage(requireContext()),
                                length = Snackbar.LENGTH_SHORT,
                                buttonText = getString(R.string.ok)
                            )
                        }
                        hideKeyboard()
                        binding.txtLayBio.clearFocus()
                        binding.txtLayFullname.clearFocus()
                    }
                    is EventMessageStatus.Failed ->
                    {
                        setLoadingState(false)
                        it.eventMessage.getContentIfNotHandled()?.let { message ->
                            binding.host.showSnackbarGravity(
                                message = message.getFormattedMessage(requireContext()),
                                length = Snackbar.LENGTH_SHORT,
                                buttonText = getString(R.string.ok)
                            )
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.newImageUri.collectLatest {
                it?.let { u ->


                    val request = ImageRequest.Builder(requireContext())
                        .data(u)
                        .target { drawable ->
                            binding.imgAvatar.setImageDrawable(drawable)
                        }
                        .build()

                    imageLoader.enqueue(request)
                }
            }
        }
    }


    private fun setupView()
    {
        binding.edTxtBio.doAfterTextChanged {
            viewModel.updateBio(it.toString())
        }

        binding.edTxtFullname.doAfterTextChanged {
            viewModel.updateFullname(it.toString())
        }

        binding.butChangeEmail.setOnClickListener {
            findNavController().navigate(EditProfileFragmentDirections.actionEditProfileFragmentToChangeEmailFragment())
        }

        binding.butChangePasswd.setOnClickListener {
            findNavController().navigate(EditProfileFragmentDirections.actionEditProfileFragmentToChangePasswdFragment())
        }

        binding.butChangeProfilePhoto.setOnClickListener {
            val items = arrayOf(
                getString(R.string.take_image_from_gallery),
                getString(R.string.make_new_image),
                getString(R.string.use_jdentIcon)
            )
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.take_image_from))
                .setItems(items) { _, item ->
                    when (item)
                    {
                        0 -> takeImageFromGallery()
                        1 -> makeNewImage()
                        2 -> makeJdenticon()
                    }
                }
                .show()
        }
    }

    private fun makeJdenticon()
    {

    }

    private fun makeNewImage()
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

    private fun takeImageFromGallery()
    {

    }

    private fun setLoadingState(isLoading: Boolean)
    {
        with(binding)
        {
            progressBarUpdate.isVisible = isLoading
            root.alpha = if (isLoading) 0.5f else 1f
            root.setViewAndChildrenEnabled(!isLoading)
        }
    }
}