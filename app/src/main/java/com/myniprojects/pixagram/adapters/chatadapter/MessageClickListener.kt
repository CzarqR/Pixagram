package com.myniprojects.pixagram.adapters.chatadapter

import com.myniprojects.pixagram.model.ChatMessage

data class MessageClickListener(
    val copyText: (ChatMessage) -> Unit = {},
    val deleteMessage: (ChatMessage) -> Unit = {},
)
