package com.myniprojects.pixagram.adapters.imageadapter

import androidx.recyclerview.widget.DiffUtil

object ImageDiffCallback : DiffUtil.ItemCallback<Image>()
{
    override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean =
            oldItem.uri == newItem.uri

    override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean =
            oldItem == newItem
}