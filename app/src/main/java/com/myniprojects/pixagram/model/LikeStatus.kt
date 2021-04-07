package com.myniprojects.pixagram.model

data class LikeStatus(
    val isPostLikeByLoggedUser: Boolean = false,
    val likeCounter: Long = 0L
)
