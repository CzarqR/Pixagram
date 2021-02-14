package com.myniprojects.pixagram.adapters.postadapter

import androidx.recyclerview.widget.DiffUtil
import com.myniprojects.pixagram.model.Post

object PostDiffCallback : DiffUtil.ItemCallback<PostWithId>()
{
    override fun areItemsTheSame(
        oldItem: PostWithId,
        newItem: PostWithId
    ): Boolean =
            oldItem.first == newItem.first

    override fun areContentsTheSame(
        oldItem: PostWithId,
        newItem: PostWithId
    ): Boolean =
            oldItem.second == newItem.second

}