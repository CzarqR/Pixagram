package com.myniprojects.pixagram.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myniprojects.pixagram.model.ChatMessage
import com.myniprojects.pixagram.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{

    val messageText: MutableLiveData<String> = MutableLiveData()

    @ExperimentalCoroutinesApi
    fun getMessages(userId: String) = repository.getMessages(userId)

    @ExperimentalCoroutinesApi
    fun sendMessage(userId: String)
    {
        messageText.value?.let {
            if (it.isNotBlank())
            {
                val msg = ChatMessage(
                    textContent = it,
                    time = System.currentTimeMillis(),
                    imageUrl = null,
                )

                viewModelScope.launch {
                    repository.sendMessage(userId, msg).collectLatest { status ->
                        Timber.d("Send msg status: $status")
                    }

                }
            }
        }

    }
}