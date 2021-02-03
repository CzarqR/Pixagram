package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.myniprojects.pixagram.repository.RealtimeDatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class MainViewModel(
    private val repository: RealtimeDatabaseRepository
) : ViewModel()
{
    val user: StateFlow<FirebaseUser?> = repository.user
}