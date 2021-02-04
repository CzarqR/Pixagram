package com.myniprojects.pixagram.vm

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.repository.RealtimeDatabaseRepository

class ProfileViewModel @ViewModelInject constructor(
    private val repository: RealtimeDatabaseRepository
) : ViewModel()
{
    fun signOutUser() = repository.signOut()
}