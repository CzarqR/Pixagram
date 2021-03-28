package com.myniprojects.pixagram.vm

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
) : ViewModelPost(repository)
{
    private val _isInfoShown: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isInfoShown = _isInfoShown.asStateFlow()

    fun changeCollapse()
    {
        _isInfoShown.value = !_isInfoShown.value
    }

    private val _likeStatus: MutableStateFlow<GetStatus<LikeStatus>> = MutableStateFlow(GetStatus.Loading)
    val likeStatus = _likeStatus.asStateFlow()

    private val _userStatus: MutableStateFlow<GetStatus<User>> = MutableStateFlow(GetStatus.Loading)
    val userStatus = _userStatus.asStateFlow()

    private val _commentStatus: MutableStateFlow<GetStatus<Long>> = MutableStateFlow(GetStatus.Loading)
    val commentStatus = _commentStatus.asStateFlow()

    private var userListenerId: Int = -1
    private var likeListenerId: Int = -1
    private var commentListenerId: Int = -1

    fun initPost(post: PostWithId)
    {
        viewModelScope.launch {
            likeListenerId = FirebaseRepository.likeListenerId
            repository.getPostLikes(likeListenerId, post.first).collectLatest {
                _likeStatus.value = it
            }
        }

        viewModelScope.launch {
            userListenerId = FirebaseRepository.userListenerId
            repository.getUser(userListenerId, post.second.owner).collectLatest {
                _userStatus.value = it
            }
        }

        viewModelScope.launch {
            commentListenerId = FirebaseRepository.commentCounterListenerId
            repository.getCommentsCounter(commentListenerId, post.first).collectLatest {
                _commentStatus.value = it
            }
        }
    }

    override fun onCleared()
    {
        super.onCleared()
        repository.removeUserListener(userListenerId)
        repository.removeLikeListener(likeListenerId)
        repository.removeCommentCounterListener(commentListenerId)
    }
}