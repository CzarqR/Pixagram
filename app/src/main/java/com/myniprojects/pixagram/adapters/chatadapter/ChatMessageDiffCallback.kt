package com.myniprojects.pixagram.adapters.chatadapter

import androidx.recyclerview.widget.DiffUtil

object ChatMessageDiffCallback : DiffUtil.ItemCallback<ChatMessageData>()
{
    override fun areItemsTheSame(oldItem: ChatMessageData, newItem: ChatMessageData): Boolean=
            oldItem.chatMessage.id == newItem.chatMessage.id

    override fun areContentsTheSame(oldItem: ChatMessageData, newItem: ChatMessageData): Boolean=
            oldItem == newItem

}
