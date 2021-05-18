package com.myniprojects.pixagram.vm

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.FollowStatus
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.utils.status.SearchFollowStatus
import com.myniprojects.pixagram.vm.utils.ViewModelPost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModelPost(repository)
{
    private val _userNotFound = MutableStateFlow(false)
    val userNotFound = _userNotFound.asStateFlow()

    private val _selectedUser: MutableStateFlow<User?> = MutableStateFlow(null)
    val selectedUser = _selectedUser.asStateFlow()

    private val _loggedUserFollowing = repository.loggedUserFollowing

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asStateFlow()

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
        _isInitialized.value = true

        /**
         * [_selectedUser] and posts are not updated
         * if in future it will be necessary listeners have to be added
         */
        _selectedUser.value = user

        viewModelScope.launch {
            repository.getUserPostsFlow(user.id).collectLatest {
                _uploadedPosts.value = it
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

        /**
         * liked and mentioned posts are loaded at the beginning
         * it is slower but ViewPager looks much better
         * when sliding for the first time
         */
        viewModelScope.launch {
            repository.getMentionedPosts(user.usernameComparator).collectLatest {
                _mentionPosts.value = it
            }
        }

        viewModelScope.launch {
            repository.getLikedPostByUserId(user.id).collectLatest {
                _likedPosts.value = it
            }
        }


    }

    @ExperimentalCoroutinesApi
    fun initWithLoggedUser() = initWithUserId(repository.requireUser.uid)

    @ExperimentalCoroutinesApi
    fun initWithUserId(userId: String)
    {
        _isInitialized.value = true

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
        _isInitialized.value = true

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


    private val _uploadedPosts: MutableStateFlow<GetStatus<List<PostWithId>>> = MutableStateFlow(
        GetStatus.Sleep
    )
    val uploadedPosts = _uploadedPosts.asStateFlow()

    private var _mentionPosts: MutableStateFlow<GetStatus<List<PostWithId>>> = MutableStateFlow(
        GetStatus.Sleep
    )
    val mentionPosts = _mentionPosts.asStateFlow()

    private var _likedPosts: MutableStateFlow<GetStatus<List<PostWithId>>> = MutableStateFlow(
        GetStatus.Sleep
    )
    val likedPosts = _likedPosts.asStateFlow()

    private val _category: MutableStateFlow<DisplayPostCategory> = MutableStateFlow(
        DisplayPostCategory.UPLOADED
    )
    val category = _category.asStateFlow()

    override fun onCleared()
    {
        super.onCleared()
        removeListeners()
    }

    fun signOut() = repository.signOut()

    fun refreshUser()
    {
        _selectedUser.value?.id?.let { id ->
            FirebaseRepository.getUserById(id)
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
                                    _selectedUser.value = u
                                }
                                else
                                {
                                    Timber.d("Something went wrong with loading user data")
                                }
                            }
                            else
                            {
                                Timber.d("User not found or found to many users (should have never happened. Critical error. Many users with the same ID)")
                            }
                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            Timber.d("Init user cancelled")
                        }
                    }
                )
        }
    }

    fun getFollowers(): Flow<GetStatus<List<String>>>
    {
        val id = _selectedUser.value?.id
        return if (_isInitialized.value && id != null)
        {
            repository.getFollowers(id)
        }
        else
        {
            flow { }
        }
    }

    fun getFollowing(): Flow<GetStatus<List<String>>>
    {
        val id = _selectedUser.value?.id
        return if (_isInitialized.value && id != null)
        {
            repository.getFollowing(id)
        }
        else
        {
            flow { }
        }
    }
}

enum class IsUserFollowed
{
    UNKNOWN,
    YES,
    NO
}

enum class DisplayPostCategory(
    @StringRes val categoryName: Int
)
{
    UPLOADED(R.string.posts),
    MENTIONS(R.string.mentions),
    LIKED(R.string.liked)

}
