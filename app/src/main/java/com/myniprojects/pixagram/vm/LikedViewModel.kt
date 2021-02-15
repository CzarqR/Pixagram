package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.DataStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class LikedViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    fun isOwnAccount(userId: String): Boolean = repository.loggedUser.value?.uid == userId

    @ExperimentalCoroutinesApi
    fun getLikedPostByLoggedUser(): Flow<DataStatus<Post>> =
            repository.getLikedPostByUserId(repository.requireUser.uid)
}