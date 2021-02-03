package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.repository.RealtimeDatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class HomeViewModel(
    private val repository: RealtimeDatabaseRepository
) : ViewModel()
{
    val loggedUserFollowing = repository.loggedUserFollowing
    val postsFromFollowingUsers = repository.postsToDisplay
}