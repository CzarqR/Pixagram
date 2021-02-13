package com.myniprojects.pixagram.adapters.commentadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.myniprojects.pixagram.databinding.CommentItemBinding
import com.myniprojects.pixagram.model.Comment
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.ext.context
import timber.log.Timber

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
        comment: CommentId,
        imageLoader: ImageLoader
    )
    {
        loadUserData(
            comment, imageLoader
        )
        with(binding)
        {
            txtBody.text = comment.second.body
        }
    }

    private var _userRef: DatabaseReference? = null
    private var _userListener: ValueEventListener? = null

    private fun loadUserData(
        comment: CommentId,
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
        _userRef = FirebaseRepository.getUserDbRef(comment.second.owner)

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
                Timber.d("Loading user info ${comment.second.owner} for comment ${comment.first} cancelled")
            }

        }
        _userRef!!.addListenerForSingleValueEvent(_userListener!!)
    }
}