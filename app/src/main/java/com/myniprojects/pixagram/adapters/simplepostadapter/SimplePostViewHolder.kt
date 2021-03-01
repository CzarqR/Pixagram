package com.myniprojects.pixagram.adapters.simplepostadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.databinding.SimplePostItemBinding

class SimplePostViewHolder private constructor(
    private val binding: SimplePostItemBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): SimplePostViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = SimplePostItemBinding.inflate(layoutInflater, parent, false)
            return SimplePostViewHolder(
                binding
            )
        }
    }

    fun bind(
        post: PostWithId,
        glide: RequestManager,
        postListener: (PostWithId) -> Unit,
    )
    {
        with(binding)
        {
            glide
                .load(post.second.imageUrl)
                .into(imgPost)

            imgPost.setOnClickListener {
                postListener(post)
            }
        }
    }

}