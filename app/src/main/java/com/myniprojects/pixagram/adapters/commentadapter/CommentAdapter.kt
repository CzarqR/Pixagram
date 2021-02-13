package com.myniprojects.pixagram.adapters.commentadapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import coil.ImageLoader
import javax.inject.Inject

class CommentAdapter @Inject constructor(
    private val imageLoader: ImageLoader,
) : ListAdapter<CommentId, CommentViewHolder>(CommentDiffCallback)
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder =
            CommentViewHolder.create(parent)


    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) =
            holder.bind(
                comment = getItem(position),
                imageLoader = imageLoader
            )


}