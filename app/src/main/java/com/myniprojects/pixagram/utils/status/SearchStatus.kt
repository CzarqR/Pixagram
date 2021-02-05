package com.myniprojects.pixagram.utils.status

import com.myniprojects.pixagram.adapters.searchadapter.SearchModel

sealed class SearchStatus
{
    object Loading : SearchStatus()
    data class Success(val result: List<SearchModel>) : SearchStatus()
    object Interrupted : SearchStatus()
}