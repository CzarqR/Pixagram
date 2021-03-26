package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    val user: StateFlow<FirebaseUser?> = repository.loggedUser

    val userData: StateFlow<User> = repository.loggedUserData

    fun signOut() = repository.signOut()
}