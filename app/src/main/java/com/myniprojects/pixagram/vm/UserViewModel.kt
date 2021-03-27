package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.DataStatus
import com.myniprojects.pixagram.utils.status.FollowStatus
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
    fun isOwnAccountUsername(username: String): Boolean = repository.isOwnAccountName(username)
    fun setLikeStatus(postId: String, status: Boolean) = repository.likeDislikePost(postId, status)

    private val _userNotFound = MutableStateFlow(false)
    val userNotFound = _userNotFound.asStateFlow()

    private val _selectedUser: MutableStateFlow<User?> = MutableStateFlow(null)
    val selectedUser = _selectedUser.asStateFlow()

    private val _loggedUserFollowing = repository.loggedUserFollowing

    /**
     * This can be used only after [initUser]
     */
    private val _userPosts: MutableStateFlow<DataStatus<Post>> = MutableStateFlow(DataStatus.Loading)
    val userPosts = _userPosts.asStateFlow()

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

        viewModelScope.launch {
            repository.getUserPostsFlow(user.id).collectLatest {
                Timber.d("POST $it")
                _userPosts.value = it
            }
        }


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

    @ExperimentalCoroutinesApi
    fun initWithLoggedUser() = initWithUserId(repository.requireUser.uid)

    @ExperimentalCoroutinesApi
    fun initWithUserId(userId: String)
    {
        FirebaseRepository.getUserById(userId)
            .addListenerForSingleValueEvent(
                object : ValueEventListener
                {
                    override fun onDataChange(snapshot: DataSnapshot)
                    {
                        val users = snapshot.children.toList()
                        if (users.size == 1)
                        {
                            val u = users[0].getValue(User::class.java)

                            if (u != null)
                            {
                                initUser(u)
                            }
                            else
                            {
                                Timber.d("Something went wrong with loading user data")
                            }
                        }
                        else
                        {
                            Timber.d("User not found or found to many users (should have never happened. Critical error. Many users with the same ID)")
                            _userNotFound.value = true
                        }
                    }

                    override fun onCancelled(error: DatabaseError)
                    {
                        Timber.d("Init user cancelled")
                    }
                }
            )
    }

    @ExperimentalCoroutinesApi
    fun initWithUsername(username: String)
    {
        FirebaseRepository.getUserByName(username)
            .addListenerForSingleValueEvent(
                object : ValueEventListener
                {
                    override fun onDataChange(snapshot: DataSnapshot)
                    {
                        val users = snapshot.children.toList()
                        if (users.size == 1)
                        {
                            val u = users[0].getValue(User::class.java)

                            if (u != null)
                            {
                                initUser(u)
                            }
                            else
                            {
                                Timber.d("Something went wrong with loading user data")
                            }
                        }
                        else
                        {
                            Timber.d("User not found or found to many users (should have never happened. Critical error. Many users with the same ID)")
                            _userNotFound.value = true
                        }
                    }

                    override fun onCancelled(error: DatabaseError)
                    {
                        Timber.d("Init user cancelled")
                    }
                }
            )
    }

    private
    val _canDoFollowUnfollowOperation = MutableStateFlow(true)
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

    fun signOut() = repository.signOut()

}

enum class IsUserFollowed
{
    UNKNOWN,
    YES,
    NO
}