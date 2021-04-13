package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentChangeEmailBinding
import com.myniprojects.pixagram.utils.ext.setViewAndChildrenEnabled
import com.myniprojects.pixagram.utils.ext.showSnackbarGravity
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.EventMessageStatus
import com.myniprojects.pixagram.vm.ChangeEmailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ChangeEmailFragment : Fragment(R.layout.fragment_change_email)
{
    val viewModel: ChangeEmailViewModel by viewModels()
    private val binding by viewBinding(FragmentChangeEmailBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupCollecting()
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.updateStatus.collectLatest {
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

    }


    private fun setLoadingState(isLoading: Boolean)
    {
        with(binding)
        {
            progressBarChangeEmail.isVisible = isLoading
            root.alpha = if (isLoading) 0.5f else 1f
            root.setViewAndChildrenEnabled(!isLoading)
        }
    }
}