package com.myniprojects.pixagram.adapters.postadapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.bumptech.glide.RequestManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.imageadapter.Image
import com.myniprojects.pixagram.adapters.imageadapter.ImageViewHolder
import com.myniprojects.pixagram.adapters.searchadapter.SearchModel
import com.myniprojects.pixagram.databinding.ImageItemBinding
import com.myniprojects.pixagram.databinding.PostItemBinding
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.RealtimeDatabaseRepository
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

                        val request = ImageRequest.Builder(root.context)
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
            txtTime.text = post.second.time.toString()
        }
    }
}