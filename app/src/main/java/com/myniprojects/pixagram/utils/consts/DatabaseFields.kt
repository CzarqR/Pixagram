package com.myniprojects.pixagram.utils.consts

import com.google.firebase.database.GenericTypeIndicator
import com.myniprojects.pixagram.model.Follow
import com.myniprojects.pixagram.model.Post

object DatabaseFields
{
    // LIKES
    const val POST_LIKES_NAME = "PostLikes"

    // USERS
    const val USERS_NAME = "Users"
    const val USERS_FIELD_USERNAME = "username"
    const val USERS_FIELD_FULL_NAME = "fullName"
    const val USERS_FIELD_EMAIL = "email"
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
    const val MENTIONS_NAME = "Mentions"

    // Following
    const val FOLLOWS_NAME = "Following"
    const val FOLLOWS_FIELD_FOLLOWING = "following"
    const val FOLLOWS_FIELD_FOLLOWER = "follower"
    val followedType = object : GenericTypeIndicator<HashMap<String, Follow>?>()
    {}

}
