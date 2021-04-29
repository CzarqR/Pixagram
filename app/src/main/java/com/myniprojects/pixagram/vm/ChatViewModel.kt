package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    @ExperimentalCoroutinesApi
    fun getMessages(userId: String) = repository.getMessages(userId)
}