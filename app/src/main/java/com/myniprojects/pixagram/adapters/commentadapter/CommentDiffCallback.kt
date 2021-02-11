package com.myniprojects.pixagram.adapters.commentadapter

import androidx.recyclerview.widget.DiffUtil

object CommentDiffCallback : DiffUtil.ItemCallback<CommentId>()
{
    override fun areItemsTheSame(
        oldItem: CommentId,
        newItem: CommentId
    ): Boolean =
            oldItem.first == newItem.first

    override fun areContentsTheSame(
        oldItem: CommentId,
        newItem: CommentId
    ): Boolean =
            oldItem.second == newItem.second
}