package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myniprojects.pixagram.model.ConversationItem
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.GetStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class MessagesViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{

    private val _conversations: MutableStateFlow<GetStatus<List<ConversationItem>>> = MutableStateFlow(
        GetStatus.Sleep
    )
    val conversation = _conversations.asStateFlow()

    private fun getConversations()
    {
        viewModelScope.launch {
            repository.getAllConversations().collectLatest { status ->
                when (status)
                {
                    GetStatus.Sleep ->
                    {
                        _conversations.value = GetStatus.Sleep
                    }
                    GetStatus.Loading ->
                    {
                        _conversations.value = GetStatus.Loading
                    }
                    is GetStatus.Failed ->
                    {
                        _conversations.value = GetStatus.Failed(status.message)
                    }
                    is GetStatus.Success ->
                    {
                        _conversations.value = GetStatus.Success(status.data.sortedByDescending { it.lastMessage.time })
                    }
                }
                Timber.d(status.toString())
            }
        }
    }

    fun updateConversations()
    {
        viewModelScope.launch {
            repository.getAllConversations().collectLatest { status ->
                if (status is GetStatus.Success)
                {
                    _conversations.value = GetStatus.Success(status.data.sortedByDescending { it.lastMessage.time })
                }
            }
        }
    }

    init
    {
        getConversations()
    }
}