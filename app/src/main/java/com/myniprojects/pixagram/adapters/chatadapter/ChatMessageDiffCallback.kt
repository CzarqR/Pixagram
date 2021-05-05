package com.myniprojects.pixagram.adapters.chatadapter

import androidx.recyclerview.widget.DiffUtil

object ChatMessageDiffCallback : DiffUtil.ItemCallback<MassageModel>()
{
    override fun areItemsTheSame(oldItem: MassageModel, newItem: MassageModel): Boolean =
            oldItem.chatMessage.id == newItem.chatMessage.id

    override fun areContentsTheSame(oldItem: MassageModel, newItem: MassageModel): Boolean =
            oldItem == newItem

}
