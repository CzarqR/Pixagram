package com.myniprojects.pixagram.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.adapters.searchadapter.SearchModel
import com.myniprojects.pixagram.model.*
import com.myniprojects.pixagram.utils.Message
import com.myniprojects.pixagram.utils.consts.Constants
import com.myniprojects.pixagram.utils.consts.DatabaseFields
import com.myniprojects.pixagram.utils.consts.StorageFields
import com.myniprojects.pixagram.utils.createImage
import com.myniprojects.pixagram.utils.ext.formatQuery
import com.myniprojects.pixagram.utils.ext.normalize
import com.myniprojects.pixagram.utils.status.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor()
{
    companion object
    {
        // region references

        private val followingDbRef = Firebase.database.getReference(DatabaseFields.FOLLOWS_NAME)
        private val postsDbRef = Firebase.database.getReference(DatabaseFields.POSTS_NAME)
        private val userDbRef = Firebase.database.getReference(DatabaseFields.USERS_NAME)
        private val hashtagsDbRef = Firebase.database.reference.child(DatabaseFields.HASHTAGS_NAME)
        private val mentionsDbRef = Firebase.database.reference.child(DatabaseFields.MENTIONS_NAME)
        private val postLikesDbRef = Firebase.database.reference.child(DatabaseFields.POST_LIKES_NAME)
        private val commentsDbRef = Firebase.database.reference.child(DatabaseFields.COMMENTS_NAME)

        fun getUserDbRef(userId: String) = userDbRef.child(userId)
        fun getPostLikesDbRef(postId: String) = postLikesDbRef.child(postId)
        fun getPostUserLikesDbRef(postId: String, userId: String) =
                getPostLikesDbRef(postId).child(userId)

        fun getPostCommentDbRef(postId: String) = commentsDbRef.child(postId)

        fun getPostByIdDbRef(postId: String) = postsDbRef.child(postId)


        private val avatarsStorageRef = Firebase.storage.getReference(StorageFields.LOCATION_AVATARS)
        private val postsStorageRef = Firebase.storage.getReference(StorageFields.LOCATION_POST)

        // endregion

        // region queries

        private fun getUserFollowing(userId: String) =
                followingDbRef.orderByChild(DatabaseFields.FOLLOWS_FIELD_FOLLOWER)
                    .equalTo(userId)

        private fun getUserFollowers(userId: String) =
                followingDbRef.orderByChild(DatabaseFields.FOLLOWS_FIELD_FOLLOWING)
                    .equalTo(userId)

        private fun getUserPost(userId: String) =
                postsDbRef.orderByChild(DatabaseFields.POSTS_FIELD_OWNER)
                    .equalTo(userId)

        /**
         * Get all hashtags which are like given String
         */
        private fun getHashtags(tag: String) =
                hashtagsDbRef
                    .orderByKey()
                    .startAt(tag)
                    .endAt(tag + "\uf8ff")

        /**
         * Get hashtag which is equal to given String
         */
        private fun getHashtag(tag: String) = hashtagsDbRef
            .orderByKey()
            .equalTo(tag.normalize())


        private fun getUsers(nick: String) =
                userDbRef
                    .orderByChild(DatabaseFields.USERS_FIELD_USERNAME_COMPARATOR)
                    .startAt(nick.toLowerCase(Locale.getDefault()))
                    .endAt(nick + "\uf8ff")

        fun getUserById(userId: String) =
                userDbRef
                    .orderByChild(DatabaseFields.USERS_FIELD_ID)
                    .equalTo(userId)

        fun getUserByName(username: String) =
                userDbRef
                    .orderByChild(DatabaseFields.USERS_FIELD_USERNAME_COMPARATOR)
                    .equalTo(username.normalize())


        // endregion

        // region listenersId

        @Volatile
        var userListenerId: Int = 0
            @Synchronized get() = field++
            @Synchronized private set

        @Volatile
        var likeListenerId: Int = 0
            @Synchronized get() = field++
            @Synchronized private set

        @Volatile
        var commentCounterListenerId: Int = 0
            @Synchronized get() = field++
            @Synchronized private set

        // endregion
    }

    // region logged user

    private val auth = Firebase.auth

    private val _loggedUser = MutableStateFlow(auth.currentUser)
    val loggedUser = _loggedUser.asStateFlow()

    private val _loggedUserData = MutableStateFlow(User())
    val loggedUserData = _loggedUserData.asStateFlow()

    /**
    probably this will newer throw nullPointerException in [com.myniprojects.pixagram.ui.MainActivity]
    when [_loggedUser] becomes null, [com.myniprojects.pixagram.ui.MainActivity] should be closed
     */
    val requireUser: FirebaseUser
        get() = _loggedUser.value!!


    init
    {
        auth.addAuthStateListener {
            _loggedUser.value = it.currentUser
            it.currentUser?.let { user ->
                Timber.d("load data for new user")
                loadLoggedUserFollowing(user.uid)
                loadUserData(user.uid)
            }
        }
    }

    private var _userRef: DatabaseReference? = null
    private var _userListener: ValueEventListener? = null

    private fun loadUserData(userId: String)
    {
        _userRef?.let { ref ->
            _userListener?.let { listener ->
                ref.removeEventListener(listener)
            }
        }

        // create listener to get user data (name, avatar url)
        _userRef = getUserDbRef(userId)

        _userListener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                Timber.d("Data for user retrieved")
                snapshot.getValue(User::class.java)?.let { user ->
                    _loggedUserData.value = user
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                Timber.d("Loading user data [$userId] cancelled")
            }

        }
        _userRef!!.addValueEventListener(_userListener!!)
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

    /**
     * Check if given id is the same as logged user
     */
    fun isOwnAccountId(userId: String): Boolean =
            loggedUser.value?.uid == userId

    fun isOwnAccountName(username: String): Boolean
    {
        Timber.d("Check: $username ${_loggedUserData.value.username}")
        return _loggedUserData.value.usernameComparator == username.normalize()
    }

    // endregion

    // region posts to display for logged user in HomeFragment

    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private var loadingPostsJob: Job? = null

    private val _postsToDisplay: MutableStateFlow<List<PostWithId>> =
            MutableStateFlow(listOf())
    val postsToDisplay = _postsToDisplay.asStateFlow()

    private val _followingUserPostQueries = hashMapOf<String, Pair<Query, ValueEventListener>>()

    val arePostsLoading = MutableStateFlow(true)

    private fun loadPosts(users: List<String>)
    {
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

        users.forEach { user ->
            if (!_followingUserPostQueries.containsKey(user)) // previous list doesn't contain user
            {
                newUsers.add(user)
            }
        }

        Timber.d("New users $newUsers")

        if (newUsers.size > 0)
        {
            loadingPostsJob = scope.launch(Dispatchers.IO) {

                arePostsLoading.value = postsToDisplay.value.isEmpty()

                var counter = 0

                newUsers.forEach { userId ->
                    Timber.d("Make query for user $userId")

                    val q = getUserPost(userId)

                    val listener = object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            counter++

                            val posts = snapshot.getValue(DatabaseFields.postsType)
                            if (posts != null)
                            {
                                /**
                                 * this will make posts sorted by posted time
                                 * in future it should be changed. Probably (I am sure)
                                 * it is not the optimal way to keep sorted list
                                 */
                                posts.putAll(_postsToDisplay.value.toMap())


                                _postsToDisplay.value = posts.toList().sortedWith(
                                    compareByDescending { pair ->
                                        pair.second.time
                                    }
                                )
                            }

                            arePostsLoading.value = postsToDisplay.value.isEmpty() || counter != newUsers.size

                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            Timber.d("Listening posts for user $userId cancelled")
                            counter++
                            arePostsLoading.value = postsToDisplay.value.isEmpty() || counter != newUsers.size
                        }

                    }

                    q.addListenerForSingleValueEvent(listener)

                    _followingUserPostQueries[userId] = q to listener
                }
            }
        }
    }

    // endregion

    // region login/register

    @ExperimentalCoroutinesApi
    fun loginUser(
        email: String?,
        passwd: String?
    ): Flow<FirebaseStatus> = channelFlow {

        send(FirebaseStatus.Loading)

        /**
        [email] and [passwd] should be already trimmed but to be sure do it again
         */
        val e = email?.trim()
        val p = passwd?.trim()


        if (e.isNullOrBlank()) // empty email
        {
            send(FirebaseStatus.Failed(Message(R.string.empty_email)))
        }
        else if (p.isNullOrBlank() || p.length < Constants.PASSWD_MIN_LENGTH) // too short passwd
        {
            send(
                FirebaseStatus.Failed(
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
                        send(FirebaseStatus.Success(Message(R.string.successful_login)))
                        close()
                    }
                }
                .addOnFailureListener {
                    launch {
                        send(FirebaseStatus.Failed(Message(R.string.wrong_email_or_passwd)))
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
    ): Flow<FirebaseStatus> = channelFlow {

        send(FirebaseStatus.Loading)

        val e = email?.trim()
        val u = username?.trim()
        val p = passwd?.trim()
        val pc = passwdConf?.trim()
        val fn = fullname?.trim()

        if (e.isNullOrBlank()) // empty email
        {
            send(FirebaseStatus.Failed(Message(R.string.empty_email)))
        }
        else if (u.isNullOrBlank() || u.length < Constants.USERNAME_MIN_LENGTH) // too short username
        {
            send(
                FirebaseStatus.Failed(
                    Message(
                        R.string.invalid_username,
                        listOf(Constants.USERNAME_MIN_LENGTH)
                    )
                )
            )

        }
        else if (!DatabaseFields.USERS_FIELD_USERNAME_PATTERN.toRegex().matches(u))
        {
            send(
                FirebaseStatus.Failed(
                    Message(R.string.invalid_username_characters)
                )
            )
        }
        else if (p.isNullOrBlank() || p.length < Constants.PASSWD_MIN_LENGTH) // too short passwd
        {
            send(
                FirebaseStatus.Failed(
                    Message(
                        R.string.invalid_password,
                        listOf(Constants.PASSWD_MIN_LENGTH)
                    )
                )
            )
        }
        else if (p != pc) //passwords are different
        {
            send(FirebaseStatus.Failed(Message(R.string.diff_passwd)))
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
                                                                                FirebaseStatus.Failed(
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
                                                                        DatabaseFields.USERS_FIELD_USERNAME_COMPARATOR to u.toLowerCase(
                                                                            Locale.getDefault()
                                                                        ),
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
                                                                                Timber.d("Success all data added")
                                                                                send(
                                                                                    FirebaseStatus.Success(
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
                                                                                    FirebaseStatus.Failed(
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
                                                                    FirebaseStatus.Failed(
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
                                                        send(FirebaseStatus.Failed(Message(R.string.username_already_used)))
                                                        close()
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError)
                                            {
                                                Timber.d("Checking username in db cancelled")
                                                launch {
                                                    send(FirebaseStatus.Failed(Message(R.string.something_went_wrong)))
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
                                    send(FirebaseStatus.Failed(Message(R.string.email_already_used)))
                                    close()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            Timber.d("Checking email in db cancelled")
                            launch {
                                send(FirebaseStatus.Failed(Message(R.string.something_went_wrong)))
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

    // region search

    @ExperimentalCoroutinesApi
    fun searchTag(query: String): Flow<SearchStatus> = channelFlow {

        send(SearchStatus.Loading)

        Timber.d("SearchTag $query")

        val text = query.formatQuery()
        if (text.isEmpty()) // tag cannot be empty because search is based on key, not value
        {
            send(SearchStatus.Interrupted)
            close()
        }
        else
        {
            getHashtags(text).addListenerForSingleValueEvent(
                object : ValueEventListener
                {
                    override fun onDataChange(snapshot: DataSnapshot)
                    {
                        val u = mutableListOf<SearchModel>()

                        snapshot.children.forEach { dataSnapshot ->
                            dataSnapshot.key?.let { key ->
                                u.add(SearchModel.TagItem(Tag(key, dataSnapshot.childrenCount)))
                            }
                        }

                        launch {
                            send(SearchStatus.Success(u))
                            close()
                        }
                    }

                    override fun onCancelled(error: DatabaseError)
                    {
                        Timber.d("SearchTag for query: `$query` was canceled. ${error.message}")
                        launch {
                            send(SearchStatus.Interrupted)
                            close()
                        }
                    }
                }
            )
        }

        awaitClose()
    }


    @ExperimentalCoroutinesApi
    fun searchUser(query: String): Flow<SearchStatus> = channelFlow {

        send(SearchStatus.Loading)

        Timber.d("SearchUser $query")

        getUsers(query.formatQuery()).addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val u = mutableListOf<SearchModel>()

                    snapshot.children.forEach { dataSnapshot ->
                        dataSnapshot.getValue(User::class.java)?.let { user ->
                            u.add(SearchModel.UserItem(user))
                        }
                    }

                    launch {
                        send(SearchStatus.Success(u))
                        close()
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Timber.d("SearchUser for query `$query` was canceled")
                    launch {
                        send(SearchStatus.Interrupted)
                        close()
                    }
                }
            }
        )
        awaitClose()
    }

    // endregion

    // region following

    /**
     * probably only one listener will be used.
     * If in the future app needs more listeners at the same time
     * values have to be kept in HashMap
     */
    private var followingListener: Triple<String, Query, ValueEventListener>? = null

    fun removeFollowingListener()
    {
        followingListener?.let {
            it.second.removeEventListener(it.third)
        }
        followingListener = null
    }

    @ExperimentalCoroutinesApi
    fun getUserFollowingFlow(userId: String): Flow<SearchFollowStatus> = channelFlow {
        removeFollowingListener()

        send(SearchFollowStatus.Loading)

        val q = getUserFollowing(userId)
        val l = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                Timber.d("Selected user [$userId] following data retrieved")

                val followers = snapshot.getValue(DatabaseFields.followedType)

                if (followers != null)
                {
                    Timber.d("Selected user following [$userId] found. Count [${followers.count()}]: $followers")

                    val followingUsers = followers.map {
                        it.value.following
                    }

                    launch {
                        send(SearchFollowStatus.Success(followingUsers))
                        close()
                    }

                }
                else // user doesn't follow anyone
                {
                    Timber.d("Selected user following [$userId] is not following anyone.")
                    launch {
                        send(SearchFollowStatus.Success(listOf()))
                        close()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                /**
                 * When query is cancelled probably nothing happens in UI
                 */
                Timber.d("Selected user following [$userId] cancelled.")
            }
        }

        followingListener = Triple(userId, q, l)

        q.addValueEventListener(l)

        awaitClose()
    }

    /**
     * probably only one listener will be used.
     * If in the future app needs more listeners at the same time
     * values have to be kept in HashMap
     */
    private var followersListener: Triple<String, Query, ValueEventListener>? = null

    fun removeFollowersListener()
    {
        followersListener?.let {
            it.second.removeEventListener(it.third)
        }
        followersListener = null
    }

    @ExperimentalCoroutinesApi
    fun getUserFollowersFlow(userId: String): Flow<SearchFollowStatus> = channelFlow {

        removeFollowersListener()

        send(SearchFollowStatus.Loading)

        val q = getUserFollowers(userId)
        val l = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                Timber.d("Selected user [$userId] followers data retrieved")

                val followers = snapshot.getValue(DatabaseFields.followedType)

                if (followers != null)
                {
                    Timber.d("Selected user followers [$userId] found. Count [${followers.count()}]: $followers")

                    val followingUsers = followers.map {
                        it.value.following
                    }

                    launch {
                        send(SearchFollowStatus.Success(followingUsers))
                    }

                }
                else // user doesn't follow anyone
                {
                    Timber.d("Selected user [$userId] is not followed by anyone.")
                    launch {
                        send(SearchFollowStatus.Success(listOf()))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                /**
                 * When query is cancelled probably nothing happens in UI
                 */
                Timber.d("Selected user following [$userId] cancelled.")
            }
        }

        followersListener = Triple(userId, q, l)

        q.addValueEventListener(l)

        awaitClose()
    }


    @ExperimentalCoroutinesApi
    fun follow(userToFollowId: String): Flow<FollowStatus> = channelFlow {

        send(FollowStatus.LOADING)

        val keyToFollow = followingDbRef.push().key

        val loggedUserId = _loggedUser.value?.uid

        if (keyToFollow != null && loggedUserId != null)
        {
            val follow = hashMapOf(
                DatabaseFields.FOLLOWS_FIELD_FOLLOWING to userToFollowId,
                DatabaseFields.FOLLOWS_FIELD_FOLLOWER to loggedUserId,
            )

            followingDbRef.child(keyToFollow).setValue(follow)
                .addOnSuccessListener {
                    Timber.d("User [$userToFollowId] is followed successfully")
                    launch {
                        send(FollowStatus.SUCCESS)
                        close()
                    }
                }
                .addOnFailureListener {
                    Timber.d("User [$userToFollowId] is not followed, failed")
                    launch {
                        send(FollowStatus.FAILED)
                        close()
                    }
                }
        }
        else
        {
            Timber.d("Error, not followed. Key or logged user is null")
            launch {
                send(FollowStatus.FAILED)
                close()
            }
        }

        awaitClose()
    }


    @ExperimentalCoroutinesApi
    fun unfollow(userToUnfollowId: String): Flow<FollowStatus> = channelFlow {

        send(FollowStatus.LOADING)

        val loggedUserId = _loggedUser.value?.uid

        if (loggedUserId != null)
        {
            val q = getUserFollowing(loggedUserId)

            q.addListenerForSingleValueEvent(
                object : ValueEventListener
                {
                    override fun onDataChange(dataSnapshot: DataSnapshot)
                    {
                        dataSnapshot.children.forEach { followSnapshot ->
                            followSnapshot.getValue(Follow::class.java)?.let { follow ->
                                if (follow.following == userToUnfollowId)
                                {
                                    followSnapshot.ref.removeValue()
                                }
                            }
                        }
                        launch {
                            send(FollowStatus.SUCCESS)
                            close()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError)
                    {
                        Timber.d("Loading users that logged user follows cancelled. ${databaseError.toException()}")
                        launch {
                            send(FollowStatus.FAILED)
                            close()
                        }
                    }
                }
            )
        }
        else
        {
            Timber.d("Error, not followed. Logged user is null")
            launch {
                send(FollowStatus.FAILED)
                close()
            }
        }

        awaitClose()
    }

    // endregion

    // region posts

    @ExperimentalCoroutinesApi
    fun getUserPostsFlow(userId: String): Flow<GetStatus<List<PostWithId>>> = channelFlow {

        send(GetStatus.Loading)

        getUserPost(userId).addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val posts = snapshot.getValue(DatabaseFields.postsType)

                    if (posts != null)
                    {
                        Timber.d("Selected user posts: $posts")

                        launch {
                            send(GetStatus.Success(posts.toList()))
                            close()
                        }
                    }
                    else
                    {
                        Timber.d("Selected user has not added any posts yet")

                        launch {
                            send(GetStatus.Success(listOf<PostWithId>()))
                            close()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Timber.d("Selected user posts error ${error.toException()}")
                }
            }
        )

        awaitClose()
    }

    @ExperimentalCoroutinesApi
    fun uploadPost(
        uri: Uri,
        desc: String,
        hashtags: List<String>,
        mentions: List<String>,
        fileExtension: String
    ): Flow<FirebaseStatus> = channelFlow {

        send(FirebaseStatus.Loading)

        val logged = _loggedUser.value

        if (logged != null)
        {
            val currentTime = System.currentTimeMillis()
            val uploadPostRef = postsStorageRef
                .child("${logged.uid}_${currentTime}.$fileExtension")

            val storageTask = uploadPostRef.putFile(uri)

            storageTask.continueWithTask {
                if (it.isSuccessful)
                {
                    uploadPostRef.downloadUrl
                }
                else
                {
                    launch {
                        send(
                            FirebaseStatus.Failed(
                                Message(
                                    R.string.post_was_not_uploaded,
                                    listOf(it.exception?.localizedMessage ?: "")
                                )
                            )
                        )
                        close()
                    }
                    throw Exception(it.exception)
                }
            }.addOnSuccessListener {
                Timber.d("Success upload $it")

                val postId = postsDbRef.push().key ?: "${logged.uid}_${currentTime}"

                val post = hashMapOf(
                    DatabaseFields.POSTS_FIELD_DESC to desc,
                    DatabaseFields.POSTS_FIELD_OWNER to logged.uid,
                    DatabaseFields.POSTS_FIELD_IMAGE_URL to it.toString(),
                    DatabaseFields.POSTS_FIELD_TIME to System.currentTimeMillis()
                )

                postsDbRef.child(postId).setValue(post)
                    .addOnSuccessListener {
                        Timber.d("Success. Post saved in db")

                        /**
                         * Saving hashtags
                         */
                        if (hashtags.isNotEmpty())
                        {
                            Timber.d("Saving hashtags")

                            hashtags.forEach { tag ->
                                val tagRef = hashtagsDbRef.child(tag.toLowerCase(Locale.getDefault()))

                                val h = mapOf(
                                    postId to true
                                )
                                tagRef.updateChildren(h)

                            }
                        }
                        else
                        {
                            Timber.d("No hashtags")
                        }

                        /**
                         * Saving mentions
                         */
                        if (mentions.isNotEmpty())
                        {
                            Timber.d("Saving mentions")

                            mentions.forEach { mention ->
                                val mentionRef = mentionsDbRef.child(mention.toLowerCase(Locale.getDefault()))

                                val h = mapOf(
                                    postId to true
                                )
                                mentionRef.updateChildren(h)
                            }
                        }
                        else
                        {
                            Timber.d("No mentions")
                        }

                        launch {
                            send(FirebaseStatus.Success(Message(R.string.post_was_uploaded)))
                            close()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Timber.d("Failed to save in db")

                        launch {
                            send(
                                FirebaseStatus.Failed(
                                    Message(
                                        R.string.post_was_not_uploaded,
                                        listOf(exception.localizedMessage ?: "")
                                    )
                                )
                            )
                            close()
                        }
                    }


            }.addOnFailureListener { exception ->
                Timber.d("Failed to upload image to storage. ${exception.message}")
                launch {
                    send(
                        FirebaseStatus.Failed(
                            Message(
                                R.string.post_was_not_uploaded,
                                listOf(exception.localizedMessage ?: "")
                            )
                        )
                    )
                    close()
                }
            }
        }
        else
        {
            send(FirebaseStatus.Failed(Message(R.string.no_logged_user)))
            close()
        }

        awaitClose()
    }


    fun likeDislikePost(postId: String, like: Boolean)
    {
        Timber.d("Like dislike repository")
        if (like)
        {
            Timber.d("Like")
            getPostUserLikesDbRef(postId, requireUser.uid).setValue(true)
        }
        else
        {
            Timber.d("Dislike")
            getPostUserLikesDbRef(postId, requireUser.uid).removeValue()
        }
    }

    @ExperimentalCoroutinesApi
    fun addComment(
        postId: String,
        comment: String
    ): Flow<FirebaseStatus> = channelFlow {

        send(FirebaseStatus.Loading)

        if (comment.isBlank())
        {
            send(FirebaseStatus.Failed(Message(R.string.comment_cannot_be_empty)))
            close()
        }
        else
        {
            val ref = getPostCommentDbRef(postId)
            val id = ref.push().key

            if (id != null)
            {
                val c = hashMapOf<String, Any>(
                    DatabaseFields.COMMENT_BODY_FIELD to comment,
                    DatabaseFields.COMMENT_TIME_FIELD to System.currentTimeMillis(),
                    DatabaseFields.COMMENT_OWNER_FIELD to requireUser.uid
                )

                ref.child(id).setValue(c)
                    .addOnSuccessListener {
                        launch {
                            send(FirebaseStatus.Success(Message(R.string.comment_posted)))
                            close()
                        }
                    }
                    .addOnFailureListener {
                        launch {
                            send(FirebaseStatus.Failed(Message(R.string.something_went_wrong)))
                            close()
                        }
                    }
            }
            else
            {
                send(FirebaseStatus.Failed(Message(R.string.something_went_wrong)))
                close()
            }
        }

        awaitClose()
    }


    private var commentRef: DatabaseReference? = null
    private var commentListener: ValueEventListener? = null

    @ExperimentalCoroutinesApi
    fun getComments(postId: String): Flow<DataStatus<Comment>> = channelFlow {
        send(DataStatus.Loading)

        /**
         * remove old listener
         */
        commentListener?.let {
            commentRef?.removeEventListener(it)
        }

        commentRef = getPostCommentDbRef(postId)

        commentListener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val comments = snapshot.getValue(DatabaseFields.commentType)
                if (comments != null)
                {
                    launch {
                        send(DataStatus.Success(comments))
                    }
                }
                else // no comments yet
                {
                    Timber.d("Post has not comments yet")
                    launch {
                        send(DataStatus.Success<DataStatus<Comment>>(hashMapOf()))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                Timber.d("Loading comments for post [$postId] cancelled")
            }
        }

        commentRef!!.addValueEventListener(commentListener!!)

        awaitClose()
    }

    fun removeCommentListener()
    {
        commentListener?.let {
            commentRef?.removeEventListener(it)
        }
    }

    @ExperimentalCoroutinesApi
    fun getLikedPostByUserId(userId: String): Flow<GetStatus<List<PostWithId>>> = channelFlow {
        send(GetStatus.Loading)

        /**
         * First get id of all posts that user liked
         */
        postLikesDbRef.orderByChild(userId).equalTo(true).addListenerForSingleValueEvent(

            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {

                    val likedIds: List<String> = snapshot.children.mapNotNull {
                        it.key
                    }

                    if (likedIds.isEmpty())
                    {
                        launch {
                            send(GetStatus.Success(listOf<PostWithId>()))
                            close()
                        }

                    }

                    val postToDisplay = likedIds.size
                    var postQueried = 0
                    val posts: MutableList<PostWithId> = mutableListOf()

                    /**
                     * this function checks if all post have been queried
                     * if so, closes flow
                     */
                    fun sendDataAndCheckClose()
                    {
                        postQueried++
                        Timber.d("Post to display: $postToDisplay. Post already queried: $postQueried")

                        launch {
                            if (postQueried == postToDisplay)
                            {
                                send(GetStatus.Success(posts))
                                close()
                            }
                        }
                    }

                    /**
                     * query every post with given id
                     */
                    likedIds.forEach { id ->
                        Timber.d("Post id to show: $id")

                        getPostByIdDbRef(id).addListenerForSingleValueEvent(
                            object : ValueEventListener
                            {
                                override fun onDataChange(snapshot: DataSnapshot)
                                {
                                    val post = snapshot.getValue(Post::class.java)
                                    if (post != null)
                                    {
                                        Timber.d("Post loaded: $post")
                                        posts.add(id to post)
                                    }
                                    else
                                    {
                                        Timber.d("Something went wrong with loading post [$id]")
                                    }
                                    sendDataAndCheckClose()
                                }

                                override fun onCancelled(error: DatabaseError)
                                {
                                    Timber.d("Loading post [$id] for user [$userId] cancelled")
                                    sendDataAndCheckClose()
                                }
                            }
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Timber.d("Loading liked post for user [$userId] cancelled")
                    close()
                }
            }
        )

        awaitClose {
            Timber.d("Closed")
        }
    }

    @ExperimentalCoroutinesApi
    fun getMentionedPosts(username: String): Flow<GetStatus<List<PostWithId>>> = channelFlow {
        send(GetStatus.Loading)

        mentionsDbRef.child(username).addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    Timber.d("Snapshot $snapshot")

                    val mentionIds: List<String> = snapshot.children.mapNotNull {
                        it.key
                    }
                    if (mentionIds.isEmpty())
                    {
                        launch {
                            send(GetStatus.Success(listOf<PostWithId>()))
                            close()
                        }

                    }
                    val postToDisplay = mentionIds.size
                    var postQueried = 0
                    val posts: MutableList<PostWithId> = mutableListOf()

                    /**
                     * this function checks if all post have been queried
                     * if so, closes flow
                     */
                    fun sendDataAndCheckClose()
                    {
                        postQueried++
                        Timber.d("Post to display: $postToDisplay. Post already queried: $postQueried")

                        launch {
                            if (postQueried == postToDisplay)
                            {
                                send(GetStatus.Success(posts))
                                close()
                            }
                        }
                    }

                    /**
                     * query every post with given id
                     */
                    mentionIds.forEach { id ->
                        Timber.d("Post id to show: $id")

                        getPostByIdDbRef(id).addListenerForSingleValueEvent(
                            object : ValueEventListener
                            {
                                override fun onDataChange(snapshot: DataSnapshot)
                                {
                                    val post = snapshot.getValue(Post::class.java)
                                    if (post != null)
                                    {
                                        Timber.d("Post loaded: $post")
                                        posts.add(id to post)
                                    }
                                    else
                                    {
                                        Timber.d("Something went wrong with loading post [$id]")
                                    }
                                    sendDataAndCheckClose()
                                }

                                override fun onCancelled(error: DatabaseError)
                                {
                                    Timber.d("Loading post [$id] for user mention $username cancelled")
                                    sendDataAndCheckClose()
                                }
                            }
                        )
                    }

                }

                override fun onCancelled(error: DatabaseError)
                {
                    Timber.d("Loading mention post for user [$username] cancelled")
                    close()
                }
            }
        )

        awaitClose()
    }

    /**
     * recommended posts are just the latest posts in database
     */
    @ExperimentalCoroutinesApi
    fun findRecommendedPosts(size: Int = Constants.RECOMMENDED_POSTS_SIZE): Flow<DataStatus<Post>> =
            channelFlow {

                postsDbRef.orderByChild(DatabaseFields.POSTS_FIELD_TIME)
                    .limitToLast(size)
                    .addListenerForSingleValueEvent(
                        object : ValueEventListener
                        {
                            override fun onDataChange(snapshot: DataSnapshot)
                            {
                                val posts = snapshot.getValue(DatabaseFields.postsType)
                                if (posts != null)
                                {
                                    Timber.d("Not null")
                                    launch {

                                        /**
                                         * Remove all posts from logged user
                                         */

                                        val u = _loggedUser.value
                                        send(
                                            DataStatus.Success(
                                                if (u != null)
                                                {
                                                    val m = hashMapOf<String, Post>()
                                                    val x = posts.filterValues {
                                                        it.owner != u.uid
                                                    }
                                                    x.forEach {
                                                        m[it.key] = it.value
                                                    }
                                                    m
                                                }
                                                else
                                                {
                                                    posts
                                                }
                                            )
                                        )
                                        close()
                                    }
                                }
                                else
                                {
                                    Timber.d("Null")
                                }
                            }

                            override fun onCancelled(error: DatabaseError)
                            {
                            }

                        }
                    )

                awaitClose()
            }

    @ExperimentalCoroutinesApi
    fun getAllPostsFromTag(tag: String): Flow<GetStatus<List<PostWithId>>> = channelFlow {

        send(GetStatus.Loading)

        hashtagsDbRef.orderByKey()
            .equalTo(tag.normalize())
            .addListenerForSingleValueEvent(
                object : ValueEventListener
                {
                    override fun onDataChange(snapshot: DataSnapshot)
                    {
                        val tags = snapshot.getValue(DatabaseFields.hashtagsType)?.get(tag)?.map {
                            it.key
                        }

                        if (tags.isNullOrEmpty())
                        {
                            Timber.d("Tags are empty or null")

                            launch {
                                send(GetStatus.Failed(Message(R.string.something_went_wrong)))
                                close()
                            }
                        }
                        else
                        {

                            val postToDisplay = tags.size
                            var tagsQueried = 0
                            val posts: MutableList<PostWithId> = mutableListOf()

                            /**
                             * this function checks if all post have been queried
                             * if so, closes flow
                             */
                            fun sendDataAndCheckClose()
                            {
                                tagsQueried++
                                Timber.d("Post to display: $postToDisplay. Post already queried: $tagsQueried")

                                launch {
                                    if (tagsQueried == postToDisplay)
                                    {
                                        send(GetStatus.Success<List<PostWithId>>(posts))
                                        close()
                                    }
                                }
                            }

                            /**
                             * query every post with given id
                             */
                            tags.forEach { id ->
                                Timber.d("Post id to show: $id")

                                getPostByIdDbRef(id).addListenerForSingleValueEvent(
                                    object : ValueEventListener
                                    {
                                        override fun onDataChange(snapshot: DataSnapshot)
                                        {
                                            val post = snapshot.getValue(Post::class.java)
                                            if (post != null)
                                            {
                                                Timber.d("Post loaded: $post")
                                                posts.add(id to post)
                                            }
                                            else
                                            {
                                                Timber.d("Something went wrong with loading post [$id]")
                                            }
                                            sendDataAndCheckClose()
                                        }

                                        override fun onCancelled(error: DatabaseError)
                                        {
                                            Timber.d("Loading post [$id] for tag [$tag] cancelled")
                                            sendDataAndCheckClose()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError)
                    {
                        Timber.d("Loading tags cancelled")
                        close()
                    }

                }
            )

        awaitClose {
            Timber.d("Closed")
        }
    }

    // endregion

    // region PostAdapter data


    private val likeListeners: HashMap<Int, FirebaseListener<GetStatus<LikeStatus>>> = hashMapOf()

    @ExperimentalCoroutinesApi
    fun getPostLikes(
        ownerHash: Int,
        postId: String
    ): Flow<GetStatus<LikeStatus>>
    {

        return channelFlow {
            send(GetStatus.Loading)

            val dr = getPostLikesDbRef(postId)

            val l = object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    launch {

                        val v = GetStatus.Success(
                            LikeStatus(
                                isPostLikeByLoggedUser = snapshot.child(requireUser.uid).exists(),
                                likeCounter = snapshot.childrenCount
                            )
                        )

                        send(v)
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    launch {
                        val v = GetStatus.Failed(Message(R.string.something_went_wrong))
                        send(v)
                    }
                }
            }

            likeListeners[ownerHash] = FirebaseListener(l, dr)
            likeListeners[ownerHash]?.addListener()

            awaitClose()
        }
    }

    fun removeLikeListener(ownerHash: Int)
    {
        Timber.d("Current list ${likeListeners.keys} removing $ownerHash")

        likeListeners[ownerHash]?.removeListener()
        likeListeners.remove(ownerHash)
        Timber.d("Current list ${likeListeners.keys}")
    }

    private val userListeners: HashMap<Int, FirebaseListener<GetStatus<User>>> = hashMapOf()

    @ExperimentalCoroutinesApi
    fun getUser(
        ownerHash: Int,
        userId: String,
    ): Flow<GetStatus<User>>
    {
        return channelFlow {

            send(GetStatus.Loading)

            val dr = getUserDbRef(userId)

            val l = object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    snapshot.getValue(User::class.java)?.let { user ->
                        launch {
                            val v = GetStatus.Success(user)
                            send(v)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Timber.d("Loading user info $userId for post cancelled")
                    launch {
                        val v = GetStatus.Failed(Message(R.string.something_went_wrong))
                        send(v)
                    }
                }

            }

            userListeners[ownerHash] = FirebaseListener(l, dr)
            userListeners[ownerHash]?.addListener()

            awaitClose()
        }
    }

    fun removeUserListener(ownerHash: Int)
    {
        userListeners[ownerHash]?.removeListener()
        userListeners.remove(ownerHash)
    }

    private val commentCounterListeners: HashMap<Int, FirebaseListener<GetStatus<Long>>> = hashMapOf()

    @ExperimentalCoroutinesApi
    fun getCommentsCounter(
        ownerHash: Int,
        postId: String,
    ): Flow<GetStatus<Long>> = channelFlow {

        send(GetStatus.Loading)

        val dr = getPostCommentDbRef(postId)

        val l = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                launch {
                    val v = GetStatus.Success(snapshot.childrenCount)
                    send(v)
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                Timber.d("Loading comment counter for post [$postId] cancelled")
                launch {
                    val v = GetStatus.Failed(Message(R.string.something_went_wrong))
                    send(v)
                }
            }

        }

        commentCounterListeners[ownerHash] = FirebaseListener(l, dr)
        commentCounterListeners[ownerHash]?.addListener()

        awaitClose()
    }

    fun removeCommentCounterListener(ownerHash: Int)
    {
        commentCounterListeners[ownerHash]?.removeListener()
        commentCounterListeners.remove(ownerHash)
        Timber.d(commentCounterListeners.keys.toString())
    }

    // endregion

    // region hashtags

    @ExperimentalCoroutinesApi
    fun getTag(tag: String): Flow<GetStatus<Tag>> = channelFlow {
        send(GetStatus.Loading)

        getHashtag(tag).addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val t = snapshot.getValue(DatabaseFields.hashtagsType)
                    val counter = t?.get(tag)?.size

                    launch {

                        send(
                            if (counter == null)
                            {
                                GetStatus.Failed(Message(R.string.something_went_wrong))
                            }
                            else
                            {
                                GetStatus.Success(Tag(tag, counter.toLong()))
                            }
                        )
                        close()
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    launch {
                        send(GetStatus.Failed(Message(R.string.something_went_wrong)))
                        close()
                    }
                }
            }
        )

        awaitClose()
    }


    // endregion
}