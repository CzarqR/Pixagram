package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    @ExperimentalCoroutinesApi
    fun addComment(
        postId: String,
        comment: String
    ) = repository.addComment(postId, comment)

    @ExperimentalCoroutinesApi
    fun getComments(
        postId: String
    ) = repository.getComments(postId)

    override fun onCleared()
    {
        super.onCleared()
        repository.removeCommentListener()
    }
}