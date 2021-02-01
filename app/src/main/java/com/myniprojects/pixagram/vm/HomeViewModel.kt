package com.myniprojects.pixagram.vm

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myniprojects.pixagram.repository.RealtimeDatabaseRepository
import com.myniprojects.pixagram.utils.DatabaseFields
import kotlinx.coroutines.flow.MutableStateFlow

class HomeViewModel @ViewModelInject constructor(
    private val repository: RealtimeDatabaseRepository
) : ViewModel()
{
    val loggedUserFollowing = repository.loggedUserFollowing
    val postsFromFollowingUsers = repository.postsToDisplay
}