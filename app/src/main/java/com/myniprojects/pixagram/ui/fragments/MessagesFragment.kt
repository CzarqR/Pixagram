package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.conversationadapter.ConversationAdapter
import com.myniprojects.pixagram.databinding.FragmentMessagesBinding
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.vm.MessagesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MessagesFragment : Fragment(R.layout.fragment_messages)
{
    @Inject
    lateinit var conversationAdapter: ConversationAdapter

    private val binding by viewBinding(FragmentMessagesBinding::bind)
    private val viewModel: MessagesViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.rvConversations.adapter = conversationAdapter.apply {
            actionMessageClick = ::conversationClick
        }

        setupCollecting()
    }

    private fun conversationClick(user: User)
    {
        Timber.d("CLick conv with $user")

        val action = MessagesFragmentDirections.actionMessagesFragmentToChatFragment(
            user = user
        )
        findNavController().navigate(action)
    }

    fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.conversation.collectLatest {
                Timber.d("All conversations collected: $it")

                when (it)
                {
                    GetStatus.Sleep ->
                    {
                        binding.proBarLoadingConversations.isVisible = false
                        binding.linLayEmptyState.isVisible = false
                        binding.linLayErrorState.isVisible = false
                        binding.rvConversations.isVisible = false
                    }
                    GetStatus.Loading ->
                    {
                        binding.proBarLoadingConversations.isVisible = true
                        binding.linLayEmptyState.isVisible = false
                        binding.linLayErrorState.isVisible = false
                        binding.rvConversations.isVisible = false
                    }
                    is GetStatus.Success ->
                    {
                        binding.proBarLoadingConversations.isVisible = false
                        binding.linLayEmptyState.isVisible = it.data.isEmpty()
                        binding.rvConversations.isVisible = it.data.isNotEmpty()
                        binding.linLayErrorState.isVisible = false

                        conversationAdapter.submitList(it.data)
                    }
                    is GetStatus.Failed ->
                    {
                        binding.proBarLoadingConversations.isVisible = false
                        binding.linLayEmptyState.isVisible = false
                        binding.linLayErrorState.isVisible = true
                        binding.rvConversations.isVisible = false
                    }
                }
            }
        }
    }


    override fun onDestroyView()
    {
        super.onDestroyView()
        conversationAdapter.cancelScopes()
    }

}