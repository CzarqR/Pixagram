package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.conversationadapter.ConversationAdapter
import com.myniprojects.pixagram.databinding.FragmentMessagesBinding
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

    fun conversationClick(userId: String)
    {
        Timber.d("CLick conv with $userId")
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

                    }
                    GetStatus.Loading ->
                    {

                    }
                    is GetStatus.Success ->
                    {
                        conversationAdapter.submitList(it.data)
                    }
                    is GetStatus.Failed ->
                    {

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