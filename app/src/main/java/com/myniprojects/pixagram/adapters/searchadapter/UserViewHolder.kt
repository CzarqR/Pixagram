package com.myniprojects.pixagram.adapters.searchadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.databinding.UserItemBinding
import com.myniprojects.pixagram.model.User

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
        glide: RequestManager
    )
    {
        with(binding)
        {
            glide
                .load(user.imageUrl)
                .into(imgAvatar)

            clickListener?.let { click ->
                binding.root.setOnClickListener {
                    click(user)
                }
            }

            txtFullName.text = user.fullName
            txtUsername.text = user.username

        }
    }
}