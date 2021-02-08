package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.myniprojects.pixagram.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: FirebaseRepository
) : ViewModel()
{
    val user: StateFlow<FirebaseUser?> = repository.loggedUser
}