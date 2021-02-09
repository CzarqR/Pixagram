package com.myniprojects.pixagram.adapters.postadapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import coil.ImageLoader
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.repository.FirebaseRepository
import javax.inject.Inject

class PostAdapter @Inject constructor(
    private val imageLoader: ImageLoader,
    private val glide: RequestManager,
    private val repository: FirebaseRepository
) : ListAdapter<Pair<String, Post>, PostViewHolder>(PostDiffCallback)
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder =
            PostViewHolder.create(parent)


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) =
            holder.bind(
                post = getItem(position),
                glide = glide,
                imageLoader = imageLoader,
                loggedUserId = repository.requireUser.uid, // This adapter can only be used in MainActivity. requireUser can return null
                likeListener = repository::likeDislikePost,
                commentListener = {},
                shareListener = {},
                commentsCounterListener = {},
                likeCounterListener = {}
            )

}