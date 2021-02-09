package com.myniprojects.pixagram.adapters.postadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.bumptech.glide.RequestManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.PostItemBinding
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.ext.context
import com.myniprojects.pixagram.utils.ext.formatWithSpaces
import com.myniprojects.pixagram.utils.ext.getDateTimeFormat
import timber.log.Timber

class PostViewHolder private constructor(
    private val binding: PostItemBinding,

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


    private var _userRef: DatabaseReference? = null
    private var _userListener: ValueEventListener? = null

    private var _likesRef: DatabaseReference? = null
    private var _likesListener: ValueEventListener? = null

    private var isPostLiked = false
        set(value)
        {
            field = value

            if (value) // Post is liked
            {
                with(binding)
                {
                    (butLike as MaterialButton).apply {
                        icon = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_baseline_favorite_24
                        )
                        setIconTintResource(R.color.red_on_surface)
                    }
                }
            }
            else // post is not liked
            {
                with(binding)
                {
                    (butLike as MaterialButton).apply {
                        icon = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_outline_favorite_border_24
                        )
                        setIconTintResource(R.color.button_on_surface)
                    }

                }
            }
        }

    fun bind(
        post: Pair<String, Post>,
        glide: RequestManager,
        imageLoader: ImageLoader,
        loggedUserId: String,
        likeListener: (String, Boolean) -> Unit,
        commentListener: (String) -> Unit,
        shareListener: (String) -> Unit,
        likeCounterListener: (String) -> Unit
    )
    {
        loadUserData(post, imageLoader)

        loadLikes(post, loggedUserId)

        with(binding)
        {

            glide
                .load(post.second.imageUrl)
                .into(imgPost)

            txtDesc.text = post.second.desc

            txtComments.text = context.getString(
                R.string.comments_format,
                47
            ) // todo. load comments from db

            txtTime.text = post.second.time.getDateTimeFormat()

            butLike.setOnClickListener {
                likeListener(post.first, !isPostLiked)
            }

            butShare.setOnClickListener {
                shareListener(post.first)
            }

            butComment.setOnClickListener {
                commentListener(post.first)
            }

            txtComments.setOnClickListener {
                commentListener(post.first)
            }

            txtLikesCounter.setOnClickListener {
                likeCounterListener(post.first)
            }

            imgLikedCounter.setOnClickListener {
                likeCounterListener(post.first)
            }
        }
    }


    private fun loadLikes(
        post: Pair<String, Post>,
        loggedUserId: String
    )
    {
        _likesRef?.let { ref ->
            _likesListener?.let { listener ->
                ref.removeEventListener(listener)
            }
        }

        // create listener to get likes
        _likesRef = FirebaseRepository.getPostLikesDbRef(post.first)

        _likesListener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                Timber.d("Post info retrieved")
                isPostLiked = snapshot.child(loggedUserId).exists()
                binding.txtLikesCounter.text = snapshot.childrenCount.formatWithSpaces()
            }

            override fun onCancelled(error: DatabaseError)
            {
                Timber.d("Check if if post [${post.first}] is liked by logged user cancelled")
            }
        }

        _likesRef!!.addValueEventListener(_likesListener!!)


    }

    private fun loadUserData(
        post: Pair<String, Post>,
        imageLoader: ImageLoader
    )
    {
        // remove old listener

        _userRef?.let { ref ->
            _userListener?.let { listener ->
                ref.removeEventListener(listener)
            }
        }

        // create listener to get user data (name, avatar url)
        _userRef = FirebaseRepository.getUserDbRef(post.second.owner)

        _userListener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                Timber.d("Data for user retrieved")
                snapshot.getValue(User::class.java)?.let { user ->
                    with(binding)
                    {
                        txtOwner.text = user.username

                        val request = ImageRequest.Builder(context)
                            .data(user.imageUrl)
                            .target { drawable ->
                                imgAvatar.setImageDrawable(drawable)
                            }
                            .build()

                        imageLoader.enqueue(request)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                Timber.d("Loading user info ${post.second.owner} for post ${post.first} cancelled")
            }

        }

        _userRef!!.addListenerForSingleValueEvent(_userListener!!)
    }
}
