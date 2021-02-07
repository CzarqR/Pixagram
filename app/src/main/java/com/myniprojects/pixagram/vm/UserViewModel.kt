package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.FollowStatus
import com.myniprojects.pixagram.utils.status.PostsStatus
import com.myniprojects.pixagram.utils.status.SearchFollowStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    private val _selectedUser: MutableStateFlow<User?> = MutableStateFlow(null)
    val selectedUser = _selectedUser.asStateFlow()

    private val _loggedUserFollowing = repository.loggedUserFollowing

    /**
     * This can be used only after [initUser]
     */
    lateinit var userPosts: Flow<PostsStatus>

    /**
     * Probably, somehow, it can be changed to StateFlow
     * Now it looks bad when val and flow is used at the same time
     */
    private var _isSelectedUserFollowedByLoggedUserVal: IsUserFollowed = IsUserFollowed.UNKNOWN
    val isSelectedUserFollowedByLoggedUser: Flow<IsUserFollowed> = _selectedUser.combine(
        _loggedUserFollowing,
    ) { selected, following ->

        _isSelectedUserFollowedByLoggedUserVal = if (selected == null)
        {
            IsUserFollowed.UNKNOWN
        }
        else
        {
            if (following.contains(selected.id)) IsUserFollowed.YES
            else IsUserFollowed.NO
        }
        _isSelectedUserFollowedByLoggedUserVal
    }

    private val _userFollowersFlow: MutableStateFlow<SearchFollowStatus> =
            MutableStateFlow(SearchFollowStatus.Sleep)
    val userFollowersFlow = _userFollowersFlow.asStateFlow()

    private val _userFollowingFlow: MutableStateFlow<SearchFollowStatus> =
            MutableStateFlow(SearchFollowStatus.Sleep)
    val userFollowingFlow = _userFollowingFlow.asStateFlow()

    @ExperimentalCoroutinesApi
    fun initUser(user: User)
    {
        /**
         * [_selectedUser] and [userPosts] are not updated
         * if in future it will be necessary listeners have to be added
         */
        _selectedUser.value = user

        userPosts = repository.getUserPostsFlow(user.id)

        viewModelScope.launch {
            repository.getUserFollowersFlow(user.id).collectLatest {
                _userFollowersFlow.value = it
            }
        }

        viewModelScope.launch {
            repository.getUserFollowingFlow(user.id).collectLatest {
                _userFollowingFlow.value = it
            }
        }
    }

    private val _canDoFollowUnfollowOperation = MutableStateFlow(true)
    val canDoFollowUnfollowOperation = _canDoFollowUnfollowOperation.asStateFlow()

    @ExperimentalCoroutinesApi
    fun followUnfollow()
    {
        if (_canDoFollowUnfollowOperation.value)
        {
            when (_isSelectedUserFollowedByLoggedUserVal)
            {
                IsUserFollowed.UNKNOWN -> Unit
                IsUserFollowed.YES -> unfollow()
                IsUserFollowed.NO -> follow()
            }
        }
        else
        {
            Timber.d("Follow unfollow in progress. Skipping request")
        }
    }


    @ExperimentalCoroutinesApi
    private fun follow()
    {
        val userToFollow = _selectedUser.value
        if (userToFollow != null)
        {
            viewModelScope.launch {
                if (_canDoFollowUnfollowOperation.value)
                {
                    repository.follow(userToFollow.id).collectLatest {
                        /**
                         * When current follow operation is in progress
                         * don't do next operation
                         */
                        _canDoFollowUnfollowOperation.value = it != FollowStatus.LOADING
                    }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun unfollow()
    {
        val userToUnfollow = _selectedUser.value
        if (userToUnfollow != null)
        {
            viewModelScope.launch {
                if (_canDoFollowUnfollowOperation.value)
                {
                    repository.unfollow(userToUnfollow.id).collectLatest {
                        /**
                         * When current unfollow operation is in progress
                         * don't do next operation
                         */
                        _canDoFollowUnfollowOperation.value = it != FollowStatus.LOADING
                    }
                }
            }
        }
    }

    /**
     * This should be called when ViewModel is destroyed
     */
    private fun removeListeners()
    {
        repository.removeFollowingListener()
        repository.removeFollowersListener()
    }

    override fun onCleared()
    {
        super.onCleared()
        removeListeners()
    }

}

enum class IsUserFollowed
{
    UNKNOWN,
    YES,
    NO
}