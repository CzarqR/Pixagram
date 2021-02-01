package com.myniprojects.pixagram.vm

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.myniprojects.pixagram.repository.RealtimeDatabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel @ViewModelInject constructor(
    private val repository: RealtimeDatabaseRepository
) : ViewModel()
{
    val user: StateFlow<FirebaseUser?> = repository.user
}