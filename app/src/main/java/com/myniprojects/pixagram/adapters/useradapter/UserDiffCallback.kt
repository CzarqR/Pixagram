package com.myniprojects.pixagram.adapters.useradapter

import androidx.recyclerview.widget.DiffUtil

object UserDiffCallback : DiffUtil.ItemCallback<String>()
{
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
}