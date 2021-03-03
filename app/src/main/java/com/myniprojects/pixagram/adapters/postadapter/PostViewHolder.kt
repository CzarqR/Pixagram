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
import com.myniprojects.pixagram.model.LikeStatus
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.ext.context
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.ext.formatWithSpaces
import com.myniprojects.pixagram.utils.ext.getDateTimeFormat
import com.myniprojects.pixagram.utils.status.GetStatus
import timber.log.Timber

typealias PostWithId = Pair<String, Post>

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
            ).apply {
                baseCommentLength = binding.context.resources.getInteger(R.integer.max_lines_post_desc)
                binding.txtDesc.setOnClickListener {
                    isCollapsed = !isCollapsed
                }
            }
        }
    }

    private var baseCommentLength = -1

    private var isCollapsed: Boolean = true
        set(value)
        {
            field = value
            binding.txtDesc.maxLines = if (value)
            {
                Timber.d("BASE $baseCommentLength")
                baseCommentLength
            }
            else
            {
                Int.MAX_VALUE
            }
        }


    private var _userRef: DatabaseReference? = null
    private var _userListener: ValueEventListener? = null

    private var _commentRef: DatabaseReference? = null
    private var _commentListener: ValueEventListener? = null

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
        post: PostWithId,
        glide: RequestManager,
        imageLoader: ImageLoader,
        likeListener: (String, Boolean) -> Unit,
        commentListener: (String) -> Unit,
        shareListener: (String) -> Unit,
        likeCounterListener: (String) -> Unit,
        profileListener: (String) -> Unit,
        imageListener: (PostWithId) -> Unit,
        tagListener: (String) -> Unit,
        linkListener: (String) -> Unit,
        mentionListener: (String) -> Unit,
    )
    {
        loadUserData(post, imageLoader)

        loadComments(post)

        isCollapsed = true

        with(binding)
        {

            glide
                .load(post.second.imageUrl)
                .into(imgPost)

            txtDesc.text = post.second.desc

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

            imgAvatar.setOnClickListener {
                profileListener(post.second.owner)
            }

            txtOwner.setOnClickListener {
                profileListener(post.second.owner)
            }

            imgPost.setOnClickListener {
                imageListener(post)
            }

            txtDesc.setOnHashtagClickListener { _, text -> tagListener(text.toString()) }
            txtDesc.setOnHyperlinkClickListener { _, text -> linkListener(text.toString()) }
            txtDesc.setOnMentionClickListener { _, text -> mentionListener(text.toString()) }

        }
    }

    fun setLikeStatus(status: GetStatus<LikeStatus>)
    {
        Timber.d("Collected $status")
        when (status)
        {
            is GetStatus.Failed ->
            {

            }
            GetStatus.Loading ->
            {
                binding.txtLikesCounter.text = binding.context.getString(R.string.loading_dots)
            }
            is GetStatus.Success ->
            {
                isPostLiked = status.data.isPostLikeByLoggedUser
                binding.txtLikesCounter.text = status.data.likeCounter.toString()
            }
        }.exhaustive
    }



    private fun loadComments(
        post: PostWithId
    )
    {
        _commentRef?.let { ref ->
            _commentListener?.let { listener ->
                ref.removeEventListener(listener)
            }
        }

        // create listener to get comments
        _commentRef = FirebaseRepository.getPostCommentDbRef(post.first)

        _commentListener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                Timber.d("Comments retrieved")
                binding.txtComments.text = binding.context.getString(
                    R.string.comments_format,
                    snapshot.childrenCount.formatWithSpaces()
                )
            }

            override fun onCancelled(error: DatabaseError)
            {
                Timber.d("Post [${post.first}] comments counter cancelled")
            }
        }

        _commentRef!!.addValueEventListener(_commentListener!!)
    }

    private fun loadUserData(
        post: PostWithId,
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
