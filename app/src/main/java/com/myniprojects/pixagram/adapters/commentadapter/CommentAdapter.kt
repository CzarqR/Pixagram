package com.myniprojects.pixagram.adapters.commentadapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import javax.inject.Inject

class CommentAdapter @Inject constructor(

) : ListAdapter<CommentId, CommentViewHolder>(CommentDiffCallback)
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder =
            CommentViewHolder.create(parent)


    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) =
            holder.bind(
                comment = getItem(position)
            )


}