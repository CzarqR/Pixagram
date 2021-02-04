package com.myniprojects.pixagram.vm

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.repository.RealtimeDatabaseRepository

//@HiltViewModel
class HomeViewModel @ViewModelInject constructor(
    private val repository: RealtimeDatabaseRepository
) : ViewModel()
{
    val loggedUserFollowing = repository.loggedUserFollowing
    val postsFromFollowingUsers = repository.postsToDisplay
}