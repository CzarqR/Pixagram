package com.myniprojects.pixagram.adapters.postadapter

import androidx.recyclerview.widget.DiffUtil
import com.myniprojects.pixagram.model.Post

object PostDiffCallback : DiffUtil.ItemCallback<Post>()
{
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem.postId == newItem.postId

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem == newItem
}