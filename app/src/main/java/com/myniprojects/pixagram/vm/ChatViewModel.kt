package com.myniprojects.pixagram.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myniprojects.pixagram.adapters.chatadapter.ChatMessageData
import com.myniprojects.pixagram.model.ChatMessage
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.FirebaseStatus
import com.myniprojects.pixagram.utils.status.GetStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class ChatViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    val messageText: MutableLiveData<String> = MutableLiveData()

    private val _allMassages: MutableStateFlow<GetStatus<List<ChatMessageData>>> = MutableStateFlow(
        GetStatus.Sleep
    )
    val allMassages = _allMassages.asStateFlow()

    private val _sendingMessageStatus: MutableStateFlow<FirebaseStatus> = MutableStateFlow(
        FirebaseStatus.Sleep
    )
    val sendingMessageStatus = _sendingMessageStatus.asStateFlow()

    private lateinit var selectedUser: User
    private lateinit var loggedUser: User


    fun initViewModel(user: User)
    {
        this.selectedUser = user
        this.loggedUser = repository.loggedUserData.value

        viewModelScope.launch {
            repository.getMessages(user.id).collectLatest { getStatus ->
                _allMassages.value = when (getStatus)
                {
                    GetStatus.Sleep ->
                    {
                        GetStatus.Sleep
                    }
                    GetStatus.Loading ->
                    {
                        GetStatus.Loading
                    }
                    is GetStatus.Success ->
                    {
                        val m: List<ChatMessageData> = getStatus.data.map { chatMsg ->
                            ChatMessageData(
                                chatMessage = chatMsg,
                                isOwnMsg = chatMsg.sender == loggedUser.id,
                                user = if (chatMsg.sender == loggedUser.id) loggedUser else selectedUser
                            )
                        }

                        GetStatus.Success(m)
                    }
                    is GetStatus.Failed ->
                    {
                        getStatus
                    }
                }

            }
        }
    }

    @ExperimentalCoroutinesApi
    fun sendMessage()
    {
        messageText.value?.let {
            if (it.isNotBlank())
            {
                val msg = ChatMessage(
                    textContent = it,
                    time = System.currentTimeMillis(),
                    imageUrl = null,
                    sender = repository.requireUser.uid
                )

                viewModelScope.launch {
                    repository.sendMessage(selectedUser.id, msg).collectLatest { status ->
                        _sendingMessageStatus.value = status
                    }
                }
            }
        }
    }
}