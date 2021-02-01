package com.myniprojects.pixagram.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myniprojects.pixagram.model.Follow
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.utils.DatabaseFields
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

class RealtimeDatabaseRepository @Inject constructor()
{
    val followedType = object : GenericTypeIndicator<HashMap<String, Follow>?>()
    {}

    val postsType = object : GenericTypeIndicator<HashMap<String, Post>>()
    {}

    // region logged user

    private val auth = Firebase.auth

    private val _user = MutableStateFlow(auth.currentUser)
    val user = _user.asStateFlow()

    /**
    probably this will newer throw nullPointerException
    when [_user] becomes null, [com.myniprojects.pixagram.ui.MainActivity] should be closed
     */
    val requireUser: FirebaseUser
        get() = _user.value!!


    init
    {
        auth.addAuthStateListener {
            _user.value = it.currentUser
            it.currentUser?.let { user ->
                loadLoggedUserData(user.uid)
            }
        }
    }

    private val _loggedUserFollowing = MutableStateFlow(listOf<String>())
    val loggedUserFollowing = _loggedUserFollowing.asStateFlow()

    private fun loadLoggedUserData(id: String)
    {
        loadLoggedUserFollowing(id)
    }

    private fun loadLoggedUserFollowing(id: String)
    {
        getUserFollowing(id).addValueEventListener(
            object : ValueEventListener
            {
                override fun onDataChange(dataSnapshot: DataSnapshot)
                {
                    dataSnapshot.getValue(followedType)?.let { followers ->
                        Timber.d("Logged user following $followers")

                        val followingUsers = followers.map {
                            it.value.following
                        }

                        loadPosts(followingUsers)

                        _loggedUserFollowing.value = followingUsers
                    }
                }

                override fun onCancelled(databaseError: DatabaseError)
                {
                    Timber.d("Loading users that logged user follows cancelled. ${databaseError.toException()}")
                }
            }
        )
    }

    // endregion

    // region posts to display

    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private var loadingPostsJob: Job? = null


    private val _postsToDisplay = MutableStateFlow(hashMapOf<String, Post>())
    val postsToDisplay = _postsToDisplay.asStateFlow()

    private val followingUserQueries = hashMapOf<String, Pair<Query, ValueEventListener>>()

    private fun loadPosts(users: List<String>)
    {
        loadingPostsJob?.cancel()
        scope.launch {
            users.forEach { userId ->
                Timber.d("Make query for user $userId")

                val q = getUserPost(userId)

                val listener = object : ValueEventListener
                {
                    override fun onDataChange(snapshot: DataSnapshot)
                    {
                        snapshot.getValue(postsType)?.let {

                            // delete this logs letter, only to test
                            Timber.d("Posts  retrieved for user $userId: $it")
                            Timber.d("Retrived count = ${it.count()}")
                            Timber.d("Curremt count = ${_postsToDisplay.value.count()}")

                            it.putAll(_postsToDisplay.value.toMap())
                            _postsToDisplay.value = it

                            Timber.d("New Retrived count = ${it.count()}")
                            Timber.d("New Curremt count = ${_postsToDisplay.value.count()}")
                        }
                    }

                    override fun onCancelled(error: DatabaseError)
                    {
                        Timber.d("Listening posts for user $userId cancelled")
                    }

                }

                q.addValueEventListener(listener)

                followingUserQueries[userId] = q to listener
            }
        }
    }

    // endregion

    // region references

    private val followingDbRef = Firebase.database.getReference(DatabaseFields.FOLLOWS_NAME)
    private val postsDbRef = Firebase.database.getReference(DatabaseFields.POSTS_NAME)


    // endregion

    // region queries

    private fun getUserFollowing(userId: String) =
            followingDbRef.orderByChild(DatabaseFields.FOLLOWS_FIELD_FOLLOWER)
                .equalTo(userId)

    private fun getUserPost(userId: String) =
            postsDbRef.orderByChild(DatabaseFields.POSTS_FIELD_OWNER)
                .equalTo(userId)


    // endregion
}