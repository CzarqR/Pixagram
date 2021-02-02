package com.myniprojects.pixagram.adapters.postadapter

import androidx.recyclerview.widget.DiffUtil
import com.myniprojects.pixagram.model.Post

object PostDiffCallback : DiffUtil.ItemCallback<Pair<String, Post>>()
{
    override fun areItemsTheSame(
        oldItem: Pair<String, Post>,
        newItem: Pair<String, Post>
    ): Boolean =
            oldItem.first == newItem.first

    override fun areContentsTheSame(
        oldItem: Pair<String, Post>,
        newItem: Pair<String, Post>
    ): Boolean =
            oldItem.second == newItem.second

}