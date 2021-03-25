package com.myniprojects.pixagram.adapters.postadapter

data class PostClickListener(
    val likeListener: (String, Boolean) -> Unit = { _, _ -> },
    val commentListener: (String) -> Unit = {},
    val shareListener: (String) -> Unit = {},
    val likeCounterListener: (String) -> Unit = {},
    val profileListener: (String) -> Unit = {},
    val imageListener: (PostWithId) -> Unit = {},
    val tagListener: (String) -> Unit = {},
    val linkListener: (String) -> Unit = {},
    val mentionListener: (String) -> Unit = {}
)


