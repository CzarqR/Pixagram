package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.chatadapter.ChatAdapter
import com.myniprojects.pixagram.databinding.FragmentChatBinding
import com.myniprojects.pixagram.utils.ext.setActionBarTitle
import com.myniprojects.pixagram.utils.ext.showSnackbarGravity
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.FirebaseStatus
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.vm.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ChatFragment : Fragment(R.layout.fragment_chat)
{
    @Inject
    lateinit var chatAdapter: ChatAdapter

    private val binding by viewBinding(FragmentChatBinding::bind)
    private val viewModel: ChatViewModel by viewModels()
    private val args: ChatFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.initViewModel(args.user)

        setupCollecting()
        setActionBarTitle(args.user.username)
        setupRecycler()
    }

    private fun setupRecycler()
    {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.reverseLayout = true
//        linearLayoutManager.stackFromEnd = true

        with(binding.rvMessages)
        {
            adapter = chatAdapter
            layoutManager = linearLayoutManager
        }

        chatAdapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver()
            {
                override fun onChanged()
                {
                    binding.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeRemoved(
                    positionStart: Int,
                    itemCount: Int
                )
                {
                    binding.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeMoved(
                    fromPosition: Int,
                    toPosition: Int,
                    itemCount: Int
                )
                {
                    binding.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeInserted(
                    positionStart: Int,
                    itemCount: Int
                )
                {
                    binding.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeChanged(
                    positionStart: Int,
                    itemCount: Int
                )
                {
                    binding.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeChanged(
                    positionStart: Int,
                    itemCount: Int,
                    payload: Any?
                )
                {
                    binding.rvMessages.smoothScrollToPosition(0)
                }
            }
        )
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.allMassages.collectLatest { status ->
                when (status)
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

                        chatAdapter.submitList(status.data)
                        binding.linLayNoMsg.isVisible = status.data.isEmpty()
//                        binding.rvMessages.post {
//                            binding.rvMessages.scrollToPosition(0)
//                        }
                    }
                    is GetStatus.Failed ->
                    {
                        binding.progressBarMessages.isVisible = false
                        binding.linLayNoMsg.isVisible = false

                        binding.cdRoot.showSnackbarGravity(
                            message = status.message.getFormattedMessage(
                                requireContext()
                            )
                        )
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.sendingMessageStatus.collectLatest {
                when (it)
                {
                    FirebaseStatus.Sleep ->
                    {
                        binding.progressBarSending.isVisible = false
                    }
                    FirebaseStatus.Loading ->
                    {
                        binding.progressBarSending.isVisible = true
                    }
                    is FirebaseStatus.Success ->
                    {
                        binding.progressBarSending.isVisible = false
                        binding.edTxtMessage.setText("")
                    }
                    is FirebaseStatus.Failed ->
                    {
                        binding.progressBarSending.isVisible = false
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