package com.myniprojects.pixagram.repository

import android.os.Build
import androidx.annotation.RequiresApi
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
                        Timber.d("Logged user following [${followers.count()}] $followers")

                        val followingUsers = followers.map {
                            it.value.following
                        }

                        _loggedUserFollowing.value = followingUsers
                        loadPosts(followingUsers)
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


    private val _postsToDisplay: MutableStateFlow<List<Pair<String, Post>>> =
            MutableStateFlow(listOf())
    val postsToDisplay = _postsToDisplay.asStateFlow()

    private val _followingUserPostQueries = hashMapOf<String, Pair<Query, ValueEventListener>>()

    private fun loadPosts(users: List<String>)
    {
        // cancel previous job
        loadingPostsJob?.cancel()

        // remove unfollowed users
        val newPosts = _postsToDisplay.value.toMutableList()
        var wasAnythingChanged = false

        _followingUserPostQueries.forEach { oldQuery ->
            if (!users.contains(oldQuery.key)) // new list doesn't contain user from old list. Remove user, query and posts
            {
                Timber.d("Remove post from user ${oldQuery.key}")
                wasAnythingChanged = true
                oldQuery.value.first.removeEventListener(oldQuery.value.second)
                newPosts.removeAll { (_, post) ->
                    post.owner == oldQuery.key
                }
            }
        }
        if (wasAnythingChanged)
        {
            _postsToDisplay.value = newPosts
        }

        // make list which contains only new users
        val newUsers = mutableListOf<String>()


        users.forEach { user ->
            if (!_followingUserPostQueries.containsKey(user)) // previous list doesn't contain user
            {
                newUsers.add(user)
            }
        }

        Timber.d("New users $newUsers")

        loadingPostsJob = scope.launch(Dispatchers.IO) {
            newUsers.forEach { userId ->
                Timber.d("Make query for user $userId")

                val q = getUserPost(userId)

                val listener = object : ValueEventListener
                {
                    override fun onDataChange(snapshot: DataSnapshot)
                    {
                        snapshot.getValue(postsType)?.let {

                            /**
                            this will make posts sorted by posted time
                            in future it should be changed. Probably (I am sure)
                            it is not the optimal way to keep sorted list
                             */
                            it.putAll(_postsToDisplay.value.toMap())
                            _postsToDisplay.value = it.toList().sortedWith(
                                compareByDescending { pair ->
                                    pair.second.time
                                }
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError)
                    {
                        Timber.d("Listening posts for user $userId cancelled")
                    }

                }

                q.addValueEventListener(listener)

                _followingUserPostQueries[userId] = q to listener
            }
        }
    }

    // endregion


    companion object
    {
        // region references

        private val followingDbRef = Firebase.database.getReference(DatabaseFields.FOLLOWS_NAME)
        private val postsDbRef = Firebase.database.getReference(DatabaseFields.POSTS_NAME)
        private val userDbRef = Firebase.database.getReference(DatabaseFields.USERS_NAME)

        fun getUserDbRef(userId: String) = userDbRef.child(userId)

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
}