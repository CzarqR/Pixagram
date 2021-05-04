package com.myniprojects.pixagram.utils.consts

import com.google.firebase.database.GenericTypeIndicator
import com.myniprojects.pixagram.model.ChatMessage
import com.myniprojects.pixagram.model.Comment
import com.myniprojects.pixagram.model.Follow
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.network.ConversationDb

object DatabaseFields
{
    // COMMENTS
    const val COMMENTS_NAME = "Comments"
    const val COMMENT_BODY_FIELD = "body"
    const val COMMENT_TIME_FIELD = "time"
    const val COMMENT_OWNER_FIELD = "owner"
    val commentType = object : GenericTypeIndicator<HashMap<String, Comment>>()
    {}

    // LIKES
    const val POST_LIKES_NAME = "PostLikes"

    // USERS
    const val USERS_NAME = "Users"
    const val USERS_FIELD_USERNAME = "username"
    const val USERS_FIELD_USERNAME_COMPARATOR = "usernameComparator"
    const val USERS_FIELD_USERNAME_PATTERN = "^[a-zA-Z0-9_]*\$"
    const val USERS_FIELD_FULL_NAME = "fullName"
    const val USERS_FIELD_EMAIL = "email" // PROBABLY THERE IS NO NEED TO KEEP EMAIL, in future try to remove it
    const val USERS_FIELD_ID = "id"
    const val USERS_FIELD_IMAGE = "imageUrl"
    const val USERS_FIELD_BIO = "bio"
    const val USERS_DEF_FIELD_BIO = "This user wants to be anonymous"
    const val USERS_DEF_FIELD_FULLNAME = ""

    // POSTS
    const val POSTS_NAME = "Posts"
    const val POSTS_FIELD_IMAGE_URL = "imageUrl"
    const val POSTS_FIELD_DESC = "desc"
    const val POSTS_FIELD_OWNER = "owner"
    const val POSTS_FIELD_TIME = "time"
    val postsType = object : GenericTypeIndicator<HashMap<String, Post>>()
    {}

    const val HASHTAGS_NAME = "Hashtags"
    val hashtagsType = object : GenericTypeIndicator<HashMap<String, HashMap<String, Boolean>>>()
    {}
    const val MENTIONS_NAME = "Mentions"

    // Following
    const val FOLLOWS_NAME = "Following"
    const val FOLLOWS_FIELD_FOLLOWING = "following"
    const val FOLLOWS_FIELD_FOLLOWER = "follower"
    val followedType = object : GenericTypeIndicator<HashMap<String, Follow>?>()
    {}

    // Messages
    const val MESSAGES_NAME = "Messages"
    const val MESSAGES_FIELD_USER_1 = "u1"
    const val MESSAGES_FIELD_USER_2 = "u2"
    const val MESSAGES_FIELD_ALL_MESSAGES = "msg"
    val conversationsType = object : GenericTypeIndicator<HashMap<String, ConversationDb>>()
    {}

    // Message
    const val MESSAGE_FIELD_TEXT_CONTENT = "textContent"
    const val MESSAGE_FIELD_IMAGE_URL = "imageUrl"
    const val MESSAGE_FIELD_TIME = "time"
    const val MESSAGE_FIELD_SENDER = "sender"
    val messageType = object : GenericTypeIndicator<HashMap<String, ChatMessage>>()
    {}

}