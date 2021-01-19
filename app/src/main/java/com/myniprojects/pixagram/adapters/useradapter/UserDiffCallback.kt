package com.myniprojects.pixagram.adapters.useradapter

import androidx.recyclerview.widget.DiffUtil
import com.myniprojects.pixagram.model.User

object UserDiffCallback : DiffUtil.ItemCallback<User>()
{
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
}