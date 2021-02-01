package com.myniprojects.pixagram.utils

object DatabaseFields
{

    // USERS
    const val USERS_NAME = "Users"
    const val USERS_FIELD_USERNAME = "username"
    const val USERS_FIELD_FULL_NAME = "fullName"
    const val USERS_FIELD_EMAIL = "email"
    const val USERS_FIELD_ID = "id"
    const val USERS_FIELD_IMAGE = "imageUrl"
    const val USERS_FIELD_BIO = "bio"
    const val USERS_DEF_FIELD_IMAGE = ""
    const val USERS_DEF_FIELD_BIO = "This user wants to be anonymous"
    const val USERS_DEF_FIELD_FULLNAME = ""

    // POSTS
    const val POSTS_NAME = "Posts"
    const val POSTS_FIELD_ID = "postId"
    const val POSTS_FIELD_IMAGE_URL = "imageUrl"
    const val POSTS_FIELD_DESC = "desc"
    const val POSTS_FIELD_OWNER = "owner"
    const val POSTS_FIELD_TIME = "time"

    const val HASHTAGS_NAME = "Hashtags"
    const val MENTIONS_NAME = "Mentions"

    // Following
    const val FOLLOWS_NAME = "Following"
    const val FOLLOWS_FIELD_FOLLOWING = "following"
    const val FOLLOWS_FIELD_FOLLOWER = "follower"

}
