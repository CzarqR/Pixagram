package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentMessagesBinding
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.vm.MessagesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MessagesFragment : Fragment(R.layout.fragment_messages)
{
    private val binding by viewBinding(FragmentMessagesBinding::bind)
    private val viewModel: MessagesViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setupCollecting()
    }

    fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.conversation.collectLatest {
                Timber.d("All conversations collected: $it")
            }
        }
    }

}