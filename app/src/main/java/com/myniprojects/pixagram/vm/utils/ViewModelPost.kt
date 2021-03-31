package com.myniprojects.pixagram.vm.utils

import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.repository.FirebaseRepository

/**
 * This view model have all functions that
 * [com.myniprojects.pixagram.ui.fragments.utils.FragmentPost] needs
 */
abstract class ViewModelPost(
    private val repository: FirebaseRepository
) : ViewModel()
{
    fun setLikeStatus(postId: String, status: Boolean) = repository.likeDislikePost(postId, status)
    fun isOwnAccountId(userId: String): Boolean = repository.isOwnAccountId(userId)
    fun isOwnAccountUsername(username: String): Boolean = repository.isOwnAccountName(username)
}