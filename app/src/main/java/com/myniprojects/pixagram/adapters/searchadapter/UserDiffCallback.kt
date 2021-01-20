package com.myniprojects.pixagram.adapters.searchadapter

import androidx.recyclerview.widget.DiffUtil

object UserDiffCallback : DiffUtil.ItemCallback<SearchModel>()
{
    override fun areItemsTheSame(oldItem: SearchModel, newItem: SearchModel): Boolean =
            if (oldItem is SearchModel.UserItem && newItem is SearchModel.UserItem)
            {
                oldItem.user.id == newItem.user.id
            }
            else if (oldItem is SearchModel.TagItem && newItem is SearchModel.TagItem)
            {
                oldItem.tag.title == newItem.tag.title
            }
            else
            {
                false
            }


    override fun areContentsTheSame(oldItem: SearchModel, newItem: SearchModel): Boolean =
            oldItem == newItem

}
