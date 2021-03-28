package com.myniprojects.pixagram.vm

import com.myniprojects.pixagram.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModelPost(repository)
{
    val postsFromFollowingUsers = repository.postsToDisplay
    val arePostsLoading = repository.arePostsLoading
    fun isOwnAccountId(userId: String): Boolean = repository.isOwnAccountId(userId)
    fun isOwnAccountUsername(username: String): Boolean = repository.isOwnAccountName(username)
}