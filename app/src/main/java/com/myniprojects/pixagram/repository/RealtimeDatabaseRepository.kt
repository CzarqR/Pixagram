package com.myniprojects.pixagram.repository

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeDatabaseRepository @Inject constructor()
{
    // region logged user

    private val auth = Firebase.auth

    private val _user = MutableStateFlow(auth.currentUser)
    val user = _user.asStateFlow()

    /**
    probably this will newer throw nullPointerException in [com.myniprojects.pixagram.ui.MainActivity]
    when [_user] becomes null, [com.myniprojects.pixagram.ui.MainActivity] should be closed
     */
    val requireUser: FirebaseUser
        get() = _user.value!!


    init
    {
        auth.addAuthStateListener {
            _user.value = it.currentUser
            it.currentUser?.let { user ->
                Timber.d("load data for new user")
                loadLoggedUserFollowing(user.uid)
            }
        }
    }

    private val _loggedUserFollowing = MutableStateFlow(listOf<String>())
    val loggedUserFollowing = _loggedUserFollowing.asStateFlow()

    private fun loadLoggedUserFollowing(id: String)
    {
        getUserFollowing(id).addValueEventListener(
            object : ValueEventListener
            {
                override fun onDataChange(dataSnapshot: DataSnapshot)
                {
                    Timber.d("onDataChanged")

                    val followers = dataSnapshot.getValue(DatabaseFields.followedType)

                    if (followers != null)
                    {
                        Timber.d("Logged user following [${followers.count()}] $followers")

                        val followingUsers = followers.map {
                            it.value.following
                        }

                        _loggedUserFollowing.value = followingUsers
                        loadPosts(followingUsers)
                    }
                    else // user doesn't follow anyone
                    {
                        Timber.d("Logged user is not following anyone")
                        _loggedUserFollowing.value = listOf()
                        loadPosts(listOf())
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
        Timber.d("Old: ${_followingUserPostQueries.keys}")
        Timber.d("New: $users")

        // cancel previous job
        loadingPostsJob?.cancel()

        // remove unfollowed users
        val newPosts = _postsToDisplay.value.toMutableList()
        val usersToRemove = mutableListOf<String>()

        _followingUserPostQueries.forEach { oldQuery ->
            if (!users.contains(oldQuery.key)) // new list doesn't contain user from old list. Remove user, query and posts
            {
                Timber.d("Remove post from user ${oldQuery.key}")
                usersToRemove.add(oldQuery.key)
                oldQuery.value.first.removeEventListener(oldQuery.value.second)
                newPosts.removeAll { (_, post) ->
                    post.owner == oldQuery.key
                }
            }
        }
        if (usersToRemove.size > 0)
        {
            _postsToDisplay.value = newPosts

            usersToRemove.forEach { idToRemove ->
                _followingUserPostQueries.remove(idToRemove)
            }
        }

        // make list which contains only new users
        val newUsers = mutableListOf<String>()

        Timber.d("Old after delete: ${_followingUserPostQueries.keys}")
        Timber.d("New: $users")

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
                        val posts = snapshot.getValue(DatabaseFields.postsType)
                        if (posts != null)
                        {
                            /**
                            this will make posts sorted by posted time
                            in future it should be changed. Probably (I am sure)
                            it is not the optimal way to keep sorted list
                             */
                            posts.putAll(_postsToDisplay.value.toMap())
                            _postsToDisplay.value = posts.toList().sortedWith(
                                compareByDescending { pair ->
                                    pair.second.time
                                }
                            )
                        }
//                        else
//                        {
//                            Timber.d("Following users by logged user doesn;t have any posts")
//                            _postsToDisplay.value = listOf()
//                        }
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


        private val avatarsStorageRef = Firebase.storage.getReference(StorageFields.LOCATION_AVATARS)


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


    // region login/register

    @ExperimentalCoroutinesApi
    fun loginUser(
        email: String?,
        passwd: String?
    ): Flow<LoginRegisterStatus> = channelFlow {

        send(LoginRegisterStatus.Loading)

        /**
        [email] and [passwd] should be already trimmed but to be sure do it again
         */
        val e = email?.trim()
        val p = passwd?.trim()


        if (e.isNullOrBlank()) // empty email
        {
            send(LoginRegisterStatus.Failed(Message(R.string.empty_email)))
        }
        else if (p.isNullOrBlank() || p.length < Constants.PASSWD_MIN_LENGTH) // too short passwd
        {
            send(
                LoginRegisterStatus.Failed(
                    Message(
                        R.string.invalid_password,
                        listOf(Constants.PASSWD_MIN_LENGTH)
                    )
                )
            )
        }
        else
        {
            auth.signInWithEmailAndPassword(e, p)
                .addOnSuccessListener {
                    launch {
                        send(LoginRegisterStatus.Success(Message(R.string.successful_login)))
                        close()
                    }
                }
                .addOnFailureListener {
                    launch {
                        send(LoginRegisterStatus.Failed(Message(R.string.wrong_email_or_passwd)))
                        close()
                    }
                }
            awaitClose()
        }
    }

    @ExperimentalCoroutinesApi
    fun registerUser(
        email: String?,
        username: String?,
        passwd: String?,
        passwdConf: String?,
        fullname: String?,
        context: Context
    ): Flow<LoginRegisterStatus> = channelFlow {

        send(LoginRegisterStatus.Loading)

        val e = email?.trim()
        val u = username?.trim()
        val p = passwd?.trim()
        val pc = passwdConf?.trim()
        val fn = fullname?.trim()

        if (e.isNullOrBlank()) // empty email
        {
            send(LoginRegisterStatus.Failed(Message(R.string.empty_email)))
        }
        else if (u.isNullOrBlank() || u.length < Constants.USERNAME_MIN_LENGTH) // too short username
        {
            send(
                LoginRegisterStatus.Failed(
                    Message(
                        R.string.invalid_username,
                        listOf(Constants.USERNAME_MIN_LENGTH)
                    )
                )
            )

        }
        else if (p.isNullOrBlank() || p.length < Constants.PASSWD_MIN_LENGTH) // too short passwd
        {
            send(
                LoginRegisterStatus.Failed(
                    Message(
                        R.string.invalid_password,
                        listOf(Constants.PASSWD_MIN_LENGTH)
                    )
                )
            )
        }
        else if (p != pc) //passwords are different
        {
            send(LoginRegisterStatus.Failed(Message(R.string.diff_passwd)))
        }
        else // can register
        {
            // check if email is already used
            userDbRef
                .orderByChild(DatabaseFields.USERS_FIELD_EMAIL)
                .equalTo(e)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            if (snapshot.childrenCount == 0L)
                            {
                                Timber.d("No email in db")

                                // check if username is already used
                                userDbRef
                                    .orderByChild(DatabaseFields.USERS_FIELD_USERNAME)
                                    .equalTo(u)
                                    .addListenerForSingleValueEvent(
                                        object : ValueEventListener
                                        {
                                            override fun onDataChange(snapshot: DataSnapshot)
                                            {
                                                if (snapshot.childrenCount == 0L)
                                                {
                                                    Timber.d("No username in db. User can be created")

                                                    auth.createUserWithEmailAndPassword(e, p)
                                                        .addOnSuccessListener { authResult ->
                                                            val newUser = authResult.user

                                                            if (newUser != null)
                                                            {
                                                                val currentTime = System.currentTimeMillis()
                                                                val avatarLocation = avatarsStorageRef
                                                                    .child("${newUser.uid}_${currentTime}.svg") // always svg

                                                                val storageTask = avatarLocation.putFile(
                                                                    createImage(context, u)
                                                                )

                                                                storageTask.continueWithTask {
                                                                    if (it.isSuccessful)
                                                                    {
                                                                        avatarLocation.downloadUrl
                                                                    }
                                                                    else
                                                                    {
                                                                        Timber.d("Something went wrong with uploading user avatar")
                                                                        launch {
                                                                            send(
                                                                                LoginRegisterStatus.Failed(
                                                                                    Message(R.string.cannot_create_user)
                                                                                )
                                                                            )
                                                                            close()
                                                                        }
                                                                        throw Exception(it.exception)
                                                                    }
                                                                }.addOnSuccessListener { imageUrl ->

                                                                    val userData = hashMapOf(
                                                                        DatabaseFields.USERS_FIELD_EMAIL to e,
                                                                        DatabaseFields.USERS_FIELD_USERNAME to u,
                                                                        DatabaseFields.USERS_FIELD_ID to newUser.uid,
                                                                        DatabaseFields.USERS_FIELD_BIO to DatabaseFields.USERS_DEF_FIELD_BIO,
                                                                        DatabaseFields.USERS_FIELD_IMAGE to imageUrl.toString(),
                                                                        DatabaseFields.USERS_FIELD_FULL_NAME to (fn
                                                                                ?: DatabaseFields.USERS_DEF_FIELD_FULLNAME),
                                                                    )

                                                                    getUserDbRef(newUser.uid)
                                                                        .setValue(userData)
                                                                        .addOnSuccessListener {
                                                                            launch {
                                                                                Timber.d("Succes all data added")
                                                                                send(
                                                                                    LoginRegisterStatus.Success(
                                                                                        Message(R.string.user_created)
                                                                                    )
                                                                                )
                                                                                close()
                                                                            }
                                                                        }
                                                                        .addOnFailureListener {
                                                                            launch {
                                                                                Timber.d("Failed data not added")
                                                                                send(
                                                                                    LoginRegisterStatus.Failed(
                                                                                        Message(R.string.cannot_save_user_into_db)
                                                                                    )
                                                                                )
                                                                                close()
                                                                            }
                                                                        }
                                                                }
                                                            }
                                                        }
                                                        .addOnFailureListener {
                                                            Timber.d("Somenthing went wrong with Auth.createUser: $it")
                                                            launch {
                                                                send(
                                                                    LoginRegisterStatus.Failed(
                                                                        Message(R.string.cannot_create_user)
                                                                    )
                                                                )
                                                                close()
                                                            }
                                                        }

                                                }
                                                else
                                                {
                                                    Timber.d("User with given username already exists")
                                                    launch {
                                                        send(LoginRegisterStatus.Failed(Message(R.string.username_already_used)))
                                                        close()
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError)
                                            {
                                                Timber.d("Checking username in db cancelled")
                                                launch {
                                                    send(LoginRegisterStatus.Failed(Message(R.string.something_went_wrong)))
                                                    close()
                                                }
                                            }
                                        }
                                    )
                            }
                            else
                            {
                                Timber.d("User with given email already exists")
                                launch {
                                    send(LoginRegisterStatus.Failed(Message(R.string.email_already_used)))
                                    close()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            Timber.d("Checking email in db cancelled")
                            launch {
                                send(LoginRegisterStatus.Failed(Message(R.string.something_went_wrong)))
                                close()
                            }
                        }
                    }
                )
            awaitClose()
        }
    }

    fun signOut()
    {
        auth.signOut()
    }

    //endregion
}