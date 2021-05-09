package com.myniprojects.pixagram.vm

import androidx.lifecycle.viewModelScope
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.vm.utils.ViewModelStateRecycler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class HomeViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModelStateRecycler(repository)
{
    private val _postToDisplay: MutableStateFlow<GetStatus<List<PostWithId>>> = MutableStateFlow(
        GetStatus.Sleep
    )
    override val postToDisplay = _postToDisplay.asStateFlow()


    fun loadPosts()
    {
        viewModelScope.launch {
            repository.getPostsFromFollowers(repository.requireUser.uid).collectLatest {
                _postToDisplay.value = it
            }
        }
    }

    init
    {
        loadPosts()
    }

    override val tryAgain: (() -> Unit)
        get() = {
            Timber.d("Try again")
            loadPosts()
        }
}