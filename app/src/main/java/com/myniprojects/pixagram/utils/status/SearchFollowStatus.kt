package com.myniprojects.pixagram.utils.status

sealed class SearchFollowStatus
{
    object Loading : SearchFollowStatus()
    data class Success(val result: List<String>) : SearchFollowStatus()
}
