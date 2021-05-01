package com.myniprojects.pixagram.adapters.chatadapter

import com.myniprojects.pixagram.model.ChatMessage
import com.myniprojects.pixagram.model.User

data class ChatMessageData(
    val chatMessage: ChatMessage,
    val user: User,
    val isOwnMsg: Boolean
)
