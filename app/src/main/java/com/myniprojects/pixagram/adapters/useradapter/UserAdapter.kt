package com.myniprojects.pixagram.adapters.useradapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.model.User
import javax.inject.Inject

class UserAdapter @Inject constructor(
    private val glide: RequestManager
) : ListAdapter<User, UserViewHolder>(UserDiffCallback)
{
    var clickListener: ((User) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder =
            UserViewHolder.from(parent)

    override fun onBindViewHolder(userViewHolder: UserViewHolder, position: Int) =
            userViewHolder.bind(
                user = getItem(position)!!,
                clickListener = clickListener,
                glide = glide
            )

}