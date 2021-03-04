package com.myniprojects.pixagram.adapters.postadapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import coil.ImageLoader
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.repository.FirebaseRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

class PostAdapter @Inject constructor(
    private val imageLoader: ImageLoader,
    private val glide: RequestManager,
    private val repository: FirebaseRepository
) : ListAdapter<PostWithId, PostViewHolder>(PostDiffCallback)
{
    var commentListener: (String) -> Unit = {}
    var profileListener: (String) -> Unit = {}
    var tagListener: (String) -> Unit = {}
    var linkListener: (String) -> Unit = {}
    var mentionListener: (String) -> Unit = {}
    var imageListener: (PostWithId) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder =
            PostViewHolder.create(parent)

    /**
     * TODO, cancel scope when recycler is detached
     */
    private val scope = CoroutineScope(
        Job() + Dispatchers.Main
    )

    @ExperimentalCoroutinesApi
    override fun onBindViewHolder(holder: PostViewHolder, position: Int)
    {
        val p = getItem(position)
        Timber.d("Value bind post: ${p.first}")
        holder.bind(
            post = p,
            glide = glide,
            imageLoader = imageLoader,
            likeListener = repository::likeDislikePost,
            commentListener = commentListener,
            shareListener = {},
            likeCounterListener = {},
            profileListener = profileListener,
            imageListener = imageListener,
            tagListener = tagListener,
            linkListener = linkListener,
            mentionListener = mentionListener
        )

        scope.launch {
            repository.getPostLikes(p.first).collectLatest {


                    holder.setLikeStatus(it)

            }
        }

        scope.launch {
            Timber.d("Value LAUNCHED for post [${p.first}]")
            repository.getUser(p.second.owner).collectLatest {
                Timber.d("Value collected for post [${p.first}] $it")


                    holder.setUserData(it)

            }
        }

        /**
         * todo, in future remove listeners when view is detached
         * maybe put in in FIFO, eg. size 50 and
         * when full remove listeners at the beginning
         */

    }
}