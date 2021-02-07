package com.myniprojects.pixagram.utils.status

sealed class SearchFollowStatus
{
    object Loading : SearchFollowStatus()
    object Sleep : SearchFollowStatus()
    data class Success(val result: List<String>) : SearchFollowStatus()
}
