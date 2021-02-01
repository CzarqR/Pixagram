package com.myniprojects.pixagram.adapters.postadapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.imageadapter.Image
import com.myniprojects.pixagram.adapters.imageadapter.ImageViewHolder
import com.myniprojects.pixagram.databinding.ImageItemBinding
import com.myniprojects.pixagram.databinding.PostItemBinding
import com.myniprojects.pixagram.model.Post

class PostViewHolder private constructor(
    private val binding: PostItemBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): PostViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = PostItemBinding.inflate(layoutInflater, parent, false)
            return PostViewHolder(
                binding
            )
        }
    }


    fun bind(
        post: Post,
        glide: RequestManager,
        imageLoader: ImageLoader
    )
    {
        with(binding)
        {
            glide
                .load(post.imageUrl)
                .into(imgPost)

            txtDesc.text = post.desc
            txtOwner.text = post.owner
        }
    }
}