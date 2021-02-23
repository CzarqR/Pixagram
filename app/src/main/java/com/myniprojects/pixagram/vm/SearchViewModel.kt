package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.ui.fragments.SearchFragment
import com.myniprojects.pixagram.utils.status.DataStatus
import com.myniprojects.pixagram.utils.status.SearchStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class SearchViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{

    // region search


    private val searchTypesArray = enumValues<SearchFragment.SearchType>()
    private var selectedSearchTypeIndex = 0

    fun selectNextSearchType()
    {
        selectedSearchTypeIndex++
        selectedSearchTypeIndex %= searchTypesArray.size
        _currentSearchType.value = searchTypesArray[selectedSearchTypeIndex]
    }

    private val _currentSearchType = MutableStateFlow(searchTypesArray[selectedSearchTypeIndex])
    val currentSearchType = _currentSearchType.asStateFlow()

    private var lastSearchType: SearchFragment.SearchType? = null

    var currentQuery: String? = null
        private set

    private var currentSearchResult: Flow<SearchStatus>? = null

    @ExperimentalCoroutinesApi
    fun search(query: String): Flow<SearchStatus>
    {
        val searchType = _currentSearchType.value
        Timber.d("Submit new query `$query`. Type $searchType")

        val lastResult = currentSearchResult

        if (query == currentQuery && _currentSearchType.value == lastSearchType && lastResult != null)
        {
            Timber.d("Returning last result")
            return lastResult
        }

        currentQuery = query
        lastSearchType = _currentSearchType.value


        val newResult: Flow<SearchStatus> = when (searchType)
        {
            SearchFragment.SearchType.USER -> repository.searchUser(query)
            SearchFragment.SearchType.TAG -> repository.searchTag(query)
        }


        currentSearchResult = newResult
        return newResult
    }

    // endregion

    // region recommended

    val recommendedPosts: Flow<DataStatus<Post>> = repository.findRecommendedPosts()

    // endregion
}