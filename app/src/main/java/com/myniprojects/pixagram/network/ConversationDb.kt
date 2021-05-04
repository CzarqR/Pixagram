package com.myniprojects.pixagram.network

import com.myniprojects.pixagram.model.ChatMessage

data class ConversationDb(
    val msg: HashMap<String, ChatMessage>? = null,
    val u1: String = "",
    val u2: String = "",
)