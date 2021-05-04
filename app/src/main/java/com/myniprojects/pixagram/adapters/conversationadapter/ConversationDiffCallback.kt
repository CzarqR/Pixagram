package com.myniprojects.pixagram.adapters.conversationadapter

import androidx.recyclerview.widget.DiffUtil
import com.myniprojects.pixagram.model.ConversationItem

object ConversationDiffCallback : DiffUtil.ItemCallback<ConversationItem>()
{
    override fun areItemsTheSame(
        oldItem: ConversationItem,
        newItem: ConversationItem
    ): Boolean =
            oldItem.userId == newItem.userId

    override fun areContentsTheSame(
        oldItem: ConversationItem,
        newItem: ConversationItem
    ): Boolean =
            oldItem == newItem

}