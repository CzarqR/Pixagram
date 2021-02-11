package com.myniprojects.pixagram.adapters.commentadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.databinding.CommentItemBinding
import com.myniprojects.pixagram.model.Comment

typealias CommentId = Pair<String, Comment>

class CommentViewHolder private constructor(
    private val binding: CommentItemBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): CommentViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = CommentItemBinding.inflate(layoutInflater, parent, false)
            return CommentViewHolder(
                binding
            )
        }
    }

    fun bind(
        comment: CommentId
    )
    {
        with(binding)
        {
            txtBody.text = comment.second.body
        }
    }
}