package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentChatBinding
import com.myniprojects.pixagram.utils.ext.setActionBarTitle
import com.myniprojects.pixagram.utils.ext.showSnackbarGravity
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.vm.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ChatFragment : Fragment(R.layout.fragment_chat)
{
    private val binding by viewBinding(FragmentChatBinding::bind)
    private val viewModel: ChatViewModel by viewModels()
    private val args: ChatFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setupCollecting()
        setActionBarTitle(args.user.username)
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.getMessages(args.user.id).collectLatest {
                when (it)
                {
                    GetStatus.Sleep ->
                    {
                        binding.progressBarMessages.isVisible = false
                        binding.linLayNoMsg.isVisible = false
                    }
                    GetStatus.Loading ->
                    {
                        binding.progressBarMessages.isVisible = true
                        binding.linLayNoMsg.isVisible = false
                    }
                    is GetStatus.Success ->
                    {
                        binding.progressBarMessages.isVisible = false

                        binding.linLayNoMsg.isVisible = it.data.isEmpty()
                    }
                    is GetStatus.Failed ->
                    {
                        binding.progressBarMessages.isVisible = false
                        binding.linLayNoMsg.isVisible = false

                        binding.cdRoot.showSnackbarGravity(
                            message = it.message.getFormattedMessage(
                                requireContext()
                            )
                        )
                    }
                }
            }
        }
    }
}