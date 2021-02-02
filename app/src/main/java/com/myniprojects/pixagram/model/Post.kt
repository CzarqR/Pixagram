package com.myniprojects.pixagram.model

data class Post(
    val desc: String = "",
    val imageUrl: String = "",
    val owner: String = "",
    val postId: String = "",
    val time: Long = 0L,
)