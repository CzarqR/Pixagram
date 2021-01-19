package com.myniprojects.pixagram.utils

object DatabaseFields
{
    // USERS
    const val USERS_NAME = "Users"
    const val USERS_FIELD_USERNAME = "username"
    const val USERS_FIELD_EMAIL = "email"
    const val USERS_FIELD_ID = "id"
    const val USERS_FIELD_IMAGE = "image_url"
    const val USERS_FIELD_BIO = "bio"
    const val USERS_DEF_FIELD_IMAGE = ""
    const val USERS_DEF_FIELD_BIO = "This user wants to be anonymous"

    // POSTS
    const val POSTS_NAME = "Posts"
    const val POSTS_ID = "postId"
    const val POSTS_IMAGE_URL = "imageUrl"
    const val POSTS_DESC = "desc"
    const val POSTS_OWNER = "owner"

    const val HASHTAGS_NAME = "Hashtags"
    const val MENTIONS_NAME = "Mentions"
}
