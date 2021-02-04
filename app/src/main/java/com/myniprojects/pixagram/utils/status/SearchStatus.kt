package com.myniprojects.pixagram.utils.status

import com.myniprojects.pixagram.adapters.searchadapter.SearchModel
import com.myniprojects.pixagram.utils.Message

sealed class SearchStatus
{
    object Loading : SearchStatus()
    data class Success(val result: List<SearchModel>) : SearchStatus()
    data class Failed(val message: Message) : SearchStatus()
}