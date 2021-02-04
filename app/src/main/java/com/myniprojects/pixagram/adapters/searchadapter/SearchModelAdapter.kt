package com.myniprojects.pixagram.adapters.searchadapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import javax.inject.Inject

class SearchModelAdapter @Inject constructor(
    private val imageLoader: ImageLoader
) : ListAdapter<SearchModel, RecyclerView.ViewHolder>(UserDiffCallback)
{
    var userListener: ((User) -> Unit)? = null
    var tagListener: ((Tag) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType)
            {
                R.layout.tag_item -> TagViewHolder.create(parent)
                R.layout.user_item -> UserViewHolder.create(parent)
                else -> throw IllegalArgumentException("Layout cannot be displayed in RecyclerView")
            }

    override fun getItemViewType(position: Int): Int =
            when (getItem(position))
            {
                is SearchModel.UserItem -> R.layout.user_item
                is SearchModel.TagItem -> R.layout.tag_item
                null -> throw UnsupportedOperationException("Unknown view")
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        getItem(position).let {
            when (it)
            {
                is SearchModel.TagItem -> (holder as TagViewHolder).bind(it.tag, tagListener)
                is SearchModel.UserItem -> (holder as UserViewHolder).bind(
                    it.user,
                    userListener,
                    imageLoader
                )
            }
        }
    }
}