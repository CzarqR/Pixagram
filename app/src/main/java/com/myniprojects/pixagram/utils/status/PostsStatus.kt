package com.myniprojects.pixagram.utils.status

import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.utils.Message

sealed class PostsStatus
{
    object Loading : PostsStatus()
    data class Success(val posts: HashMap<String, Post>) : PostsStatus()
    data class Failed(val message: Message) : PostsStatus()
}
