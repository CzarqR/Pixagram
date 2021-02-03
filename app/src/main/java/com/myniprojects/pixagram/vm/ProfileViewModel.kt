package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class ProfileViewModel : ViewModel()
{
    private val auth = Firebase.auth

    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    init
    {
        auth.addAuthStateListener {
            _user.value = it.currentUser
        }
    }

    fun signOutUser()
    {
        auth.signOut()
    }
}