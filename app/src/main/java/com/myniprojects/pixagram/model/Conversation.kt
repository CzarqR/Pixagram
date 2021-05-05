package com.myniprojects.pixagram.model

data class Conversation(
    val msg: HashMap<String, ChatMessage>? = null,
    val u1: String = "",
    val u2: String = "",
)