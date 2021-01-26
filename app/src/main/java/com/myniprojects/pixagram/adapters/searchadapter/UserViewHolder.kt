package com.myniprojects.pixagram.adapters.searchadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.databinding.UserItemBinding
import com.myniprojects.pixagram.model.User
import timber.log.Timber


class UserViewHolder private constructor(
    private val binding: UserItemBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): UserViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = UserItemBinding.inflate(layoutInflater, parent, false)
            return UserViewHolder(
                binding
            )
        }
    }


    fun bind(
        user: User,
        clickListener: ((User) -> Unit)?,
        imageLoader: ImageLoader
    )
    {
        with(binding)
        {
            Timber.d("IMG ${user.imageUrl}")

            val request = ImageRequest.Builder(root.context)
                .data(user.imageUrl)
                .target { drawable ->
                    // Handle the result.
                    imgAvatar.setImageDrawable(drawable)
                }
                .build()

            imageLoader.enqueue(request)

            clickListener?.let { click ->
                root.setOnClickListener {
                    click(user)
                }
            }

            txtFullName.text = user.fullName
            txtUsername.text = user.username
        }
    }
}