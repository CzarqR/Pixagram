package com.myniprojects.pixagram.model

data class Comment(
    val body: String = "",
    val owner: String = "",
    val time: Long = 0L
)