package com.myniprojects.pixagram.repository

import android.content.Context
import android.net.Uri
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
import com.myniprojects.pixagram.adapters.searchadapter.SearchModel
import com.myniprojects.pixagram.model.Follow
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.utils.Message
import com.myniprojects.pixagram.utils.consts.Constants
import com.myniprojects.pixagram.utils.consts.DatabaseFields
import com.myniprojects.pixagram.utils.consts.StorageFields
import com.myniprojects.pixagram.utils.createImage
import com.myniprojects.pixagram.utils.ext.formatQuery
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
        private val postDbRef = Firebase.database.reference.child(DatabaseFields.POST_LIKES_NAME)

        fun getUserDbRef(userId: String) = userDbRef.child(userId)
        fun getPostLikesDbRef(postId: String) = postDbRef.child(postId)
        fun getPostUserLikesDbRef(postId: String, userId: String) =
                getPostLikesDbRef(postId).child(userId)


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

        private fun getHashtags(tag: String) =
                hashtagsDbRef
                    .orderByKey()
                    .startAt(tag)
                    .endAt(tag + "\uf8ff")

        /**
         * TODO it is case sensitive. Not every user is found
         */
        private fun getUsers(nick: String) =
                userDbRef
                    .orderByChild(DatabaseFields.USERS_FIELD_USERNAME)
                    .startAt(nick) //this API is a fucking joke...
                    .endAt(nick + "\uf8ff")


        // endregion
    }

    // region logged user

    private val auth = Firebase.auth

    private val _loggedUser = MutableStateFlow(auth.currentUser)
    val loggedUser = _loggedUser.asStateFlow()

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

    // region posts to display for logged user in HomeFragment

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

        getHashtags(query.formatQuery()).addListenerForSingleValueEvent(
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
                    Timber.d("SearchTag for query: `$query` was canceled")
                    launch {
                        send(SearchStatus.Interrupted)
                        close()
                    }
                }
            }
        )
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
    fun getUserPostsFlow(userId: String): Flow<PostsStatus> = channelFlow {

        send(PostsStatus.Loading)

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
                            send(PostsStatus.Success(posts))
                            close()
                        }
                    }
                    else
                    {
                        Timber.d("Selected user has not added any posts yet")

                        launch {
                            send(PostsStatus.Success(hashMapOf()))
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

                                val key = tagRef.push().key

                                if (key != null)
                                {
                                    val h = mapOf(
                                        key to postId
                                    )
                                    tagRef.updateChildren(h)
                                }
                                else
                                {
                                    Timber.e("Hashtag [$tag] was not added to db. Key was null")
                                }
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

                                val key = mentionRef.push().key

                                if (key != null)
                                {
                                    val h = mapOf(
                                        key to postId
                                    )
                                    mentionRef.updateChildren(h)
                                }
                                else
                                {
                                    Timber.e("Mention [$mention] was not added to db. Key was null")
                                }
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

    // endregion
}