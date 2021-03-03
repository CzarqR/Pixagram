package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myniprojects.pixagram.model.LikeStatus
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.GetStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class DetailPostViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    private val _postStatus: MutableStateFlow<GetStatus<LikeStatus>> = MutableStateFlow(GetStatus.Loading)
    val postStatus = _postStatus.asStateFlow()

    fun initPost(postId: String)
    {
        viewModelScope.launch {
            repository.getPostLikes(postId).collectLatest {
                _postStatus.value = it
            }
        }
    }
}