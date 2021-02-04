package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.ui.fragments.SearchFragment
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.status.SearchStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    private var currentSearchType: SearchFragment.SearchType? = null
    private var currentQuery: String? = null
    private var currentSearchResult: Flow<SearchStatus>? = null

    @ExperimentalCoroutinesApi
    fun search(query: String, searchType: SearchFragment.SearchType): Flow<SearchStatus>
    {
        Timber.d("Submit new query $query. Type $searchType")

        val lastResult = currentSearchResult

        if (query == currentQuery && currentSearchType == searchType && lastResult != null)
        {
            Timber.d("Returning last result")
            return lastResult
        }

        currentQuery = query
        currentSearchType = searchType


        val newResult: Flow<SearchStatus> = when (searchType)
        {
            SearchFragment.SearchType.USER -> repository.searchUser(query)
            SearchFragment.SearchType.TAG -> repository.searchTag(query)
        }.exhaustive


        currentSearchResult = newResult
        return newResult
    }
}