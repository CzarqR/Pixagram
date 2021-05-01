package com.myniprojects.pixagram.adapters.chatadapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.RequestManager
import javax.inject.Inject

class ChatAdapter @Inject constructor(
    private val glide: RequestManager
) : ListAdapter<ChatMessageData, ChatMessageViewHolder>(ChatMessageDiffCallback)
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder =
            ChatMessageViewHolder.create(parent)

    override fun onBindViewHolder(holderMessage: ChatMessageViewHolder, position: Int) =
            holderMessage.bind(
                chatMessageData = getItem(position)!!,
                glide = glide
            )
}