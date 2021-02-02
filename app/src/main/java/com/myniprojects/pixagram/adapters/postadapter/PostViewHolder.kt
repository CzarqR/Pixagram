package com.myniprojects.pixagram.adapters.postadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.bumptech.glide.RequestManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.PostItemBinding
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.RealtimeDatabaseRepository
import com.myniprojects.pixagram.utils.context
import com.myniprojects.pixagram.utils.formatWithSpaces
import com.myniprojects.pixagram.utils.getDateTimeFormat
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


    fun bind(
        post: Pair<String, Post>,
        glide: RequestManager,
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
        _userRef = RealtimeDatabaseRepository.getUserDbRef(post.second.owner)

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

        _userListener?.let { listener ->
            _userRef?.addListenerForSingleValueEvent(listener)
        }

        with(binding)
        {

            glide
                .load(post.second.imageUrl)
                .into(imgPost)

            txtDesc.text = post.second.desc

            txtComments.text = context.getString(
                R.string.comments,
                47
            ) // todo. load comments from db
            txtLikesCounter.text = 1923L.formatWithSpaces() // todo. load likes from db

            txtTime.text = post.second.time.getDateTimeFormat()
        }
    }
}