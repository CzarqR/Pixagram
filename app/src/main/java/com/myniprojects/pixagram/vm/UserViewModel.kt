package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myniprojects.pixagram.model.Follow
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.consts.DatabaseFields
import com.myniprojects.pixagram.utils.ext.exhaustive
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
    val followedType = object : GenericTypeIndicator<HashMap<String, Follow>?>()
    {}

    val postsType = object : GenericTypeIndicator<HashMap<String, Post>>()
    {}

    private val followingDbRef = Firebase.database.getReference(DatabaseFields.FOLLOWS_NAME)
    private val loggedUser = Firebase.auth.currentUser!!

    private val _selectedUser: MutableStateFlow<User?> = MutableStateFlow(null)
    val selectedUser = _selectedUser.asStateFlow()

    private val _selectedUserFollowedBy = MutableStateFlow(listOf<String>())
    val selectedUserFollowedBy = _selectedUserFollowedBy.asStateFlow()

    private val _selectedUserFollowingCounter = MutableStateFlow(0)
    val selectedUserFollowingCounter = _selectedUserFollowingCounter.asStateFlow()

    private val _selectedUserFollowersCounter = MutableStateFlow(0)
    val selectedUserFollowersCounter = _selectedUserFollowersCounter.asStateFlow()


    private val _selectedUserPosts = MutableStateFlow(hashMapOf<String, Post>())
    val selectedUserPosts = _selectedUserPosts.asStateFlow()

    private val _loggedUserFollowing = repository.loggedUserFollowing

    private var _isFollowUnfollowStarted = MutableStateFlow(false)

    private var _isSelectedUserFollowedByLoggedUser = false
    val isSelectedUserFollowedByLoggedUser: Flow<Boolean> = _selectedUser.combine(
        _loggedUserFollowing,
    ) { selected, following ->
        selected?.let {
            _isSelectedUserFollowedByLoggedUser = following.contains(selected.id)
        }
        _isSelectedUserFollowedByLoggedUser
    }

    private lateinit var userFollowersFlow: Flow<SearchFollowStatus>

    @ExperimentalCoroutinesApi
    fun initUser(user: User)
    {
        _selectedUser.value = user

        userFollowersFlow = repository.getUserFollowersFlow(user.id)

        viewModelScope.launch {
            repository.getUserFollowingFlow(user.id).collectLatest {
                when (it)
                {
                    SearchFollowStatus.Loading ->
                    {
                    }
                    is SearchFollowStatus.Success ->
                    {
                        _selectedUserFollowingCounter.value = it.result.size
                    }
                }.exhaustive
            }
        }

        viewModelScope.launch {
            userFollowersFlow.collectLatest {
                when (it)
                {
                    SearchFollowStatus.Loading ->
                    {
                    }
                    is SearchFollowStatus.Success ->
                    {
                        _selectedUserFollowersCounter.value = it.result.size
                    }
                }.exhaustive
            }
        }

        val postDbRef = Firebase.database.getReference(DatabaseFields.POSTS_NAME)

        val qPost = postDbRef.orderByChild(DatabaseFields.POSTS_FIELD_OWNER).equalTo(user.id)

        qPost.addValueEventListener(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    Timber.d("Selected user posts retrieved received. $snapshot")
                    snapshot.getValue(postsType)?.let {
                        _selectedUserPosts.value = it
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Timber.d("Selected user posts error ${error.toException()}")
                }
            }
        )

    }


    fun followUnfollow()
    {
        Timber.d("Start")
        if (_isFollowUnfollowStarted.value)
        {
            Timber.d("Follow unfollow in progress. Skipping request")
        }
        else
        {
            _isFollowUnfollowStarted.value = true
            if (_isSelectedUserFollowedByLoggedUser)
            {
                Timber.d("Unfollow")
                unfollow()
            }
            else
            {
                Timber.d("Follow")
                follow()
            }
        }
    }

    private fun follow()
    {
        val keyToFollow = followingDbRef.push().key
        val selected = _selectedUser.value

        if (keyToFollow != null && selected != null)
        {
            val follow = hashMapOf(
                DatabaseFields.FOLLOWS_FIELD_FOLLOWING to selected.id,
                DatabaseFields.FOLLOWS_FIELD_FOLLOWER to loggedUser.uid,
            )

            followingDbRef.child(keyToFollow).setValue(follow).addOnCompleteListener {
                Timber.d("Is user followed successfully: ${it.isSuccessful}")
                _isFollowUnfollowStarted.value = false
            }
        }
        else
        {
            Timber.d("Error, not followed")
            _isFollowUnfollowStarted.value = false
        }

    }

    private fun unfollow()
    {
        val q = followingDbRef.orderByChild(DatabaseFields.FOLLOWS_FIELD_FOLLOWER)
            .equalTo(loggedUser.uid)

        q.addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(dataSnapshot: DataSnapshot)
                {
                    dataSnapshot.children.forEach { followSnapshot ->
                        followSnapshot.getValue(Follow::class.java)?.let { follow ->
                            if (follow.following == _selectedUser.value?.id)
                            {
                                followSnapshot.ref.removeValue()
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError)
                {
                    Timber.d("Loading users that logged user follows cancelled. ${databaseError.toException()}")
                }
            }
        )
        _isFollowUnfollowStarted.value = false
    }

}