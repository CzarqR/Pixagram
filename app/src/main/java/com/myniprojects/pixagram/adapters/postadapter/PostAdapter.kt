package com.myniprojects.pixagram.adapters.postadapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import coil.ImageLoader
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.repository.FirebaseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

class PostAdapter @Inject constructor(
    private val imageLoader: ImageLoader,
    private val glide: RequestManager,
    private val repository: FirebaseRepository
) : ListAdapter<PostWithId, PostViewHolder>(PostDiffCallback)
{
    init
    {
        Timber.d("onDestroy Created")
    }

    var postClickListener = object : PostClickListener
    {}


    private fun cancelListeners(
        userListenerId: Int,
        likeListenerId: Int,
        commentListenerId: Int
    )
    {
        repository.removeUserListener(userListenerId)
        repository.removeLikeListener(likeListenerId)
        repository.removeCommentCounterListener(commentListenerId)
    }

    private val holders: MutableList<() -> Unit> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder =
            PostViewHolder.create(parent, ::cancelListeners).apply {
                holders.add(::cancelJobs)
            }

    @ExperimentalCoroutinesApi
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) = holder.bind(
        post = getItem(position),
        glide = glide,
        imageLoader = imageLoader,
        postClickListener = postClickListener,
        userFlow = repository::getUser,
        likeFlow = repository::getPostLikes,
        commentCounterFlow = repository::getCommentsCounter,
    )

    fun cancelScopes()
    {
        holders.forEach { cancelScope ->
            cancelScope()
        }
    }
}