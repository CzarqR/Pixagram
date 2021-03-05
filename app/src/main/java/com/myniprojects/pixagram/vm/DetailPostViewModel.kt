package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.model.LikeStatus
import com.myniprojects.pixagram.model.User
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
    private val _likeStatus: MutableStateFlow<GetStatus<LikeStatus>> = MutableStateFlow(GetStatus.Loading)
    val likeStatus = _likeStatus.asStateFlow()

    private val _userStatus: MutableStateFlow<GetStatus<User>> = MutableStateFlow(GetStatus.Loading)
    val userStatus = _userStatus.asStateFlow()

    private var userListenerId: Int = -1

    fun initPost(post: PostWithId)
    {
        viewModelScope.launch {
            repository.getPostLikes(post.first).collectLatest {
                _likeStatus.value = it
            }
        }

        viewModelScope.launch {
            userListenerId = FirebaseRepository.userListenerId
            repository.getUser(userListenerId, post.second.owner).collectLatest {
                _userStatus.value = it
            }
        }
    }

    fun likeDislike(postId: String, like: Boolean) = repository.likeDislikePost(postId, like)

    override fun onCleared()
    {
        super.onCleared()
        repository.removeUserListener(userListenerId)
    }
}