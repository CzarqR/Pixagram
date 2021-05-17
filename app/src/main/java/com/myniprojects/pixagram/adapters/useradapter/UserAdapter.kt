package com.myniprojects.pixagram.adapters.useradapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import coil.ImageLoader
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class UserAdapter @Inject constructor(
    private val imageLoader: ImageLoader,
    private val repository: FirebaseRepository
) : ListAdapter<String, UserViewHolderById>(UserDiffCallback)
{
    lateinit var userClick: (User) -> Unit

    private val holders: MutableList<() -> Unit> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolderById =
            UserViewHolderById.create(
                parent = parent,
                cancelListeners = repository::removeUserListener,
                imageLoader = imageLoader
            ).apply {
                holders.add(::cancelJobs)
            }

    @ExperimentalCoroutinesApi
    override fun onBindViewHolder(holder: UserViewHolderById, position: Int) = holder.bind(
        userFlow = repository::getUser,
        userId = getItem(position),
        userClick = {
            userClick(it)
        }
    )

    fun cancelScopes()
    {
        holders.forEach { cancelScope ->
            cancelScope()
        }
    }
}