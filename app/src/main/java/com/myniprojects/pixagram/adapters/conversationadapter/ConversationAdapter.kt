package com.myniprojects.pixagram.adapters.conversationadapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import coil.ImageLoader
import com.myniprojects.pixagram.model.ConversationItem
import com.myniprojects.pixagram.repository.FirebaseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class ConversationAdapter @Inject constructor(
    private val imageLoader: ImageLoader,
    private val repository: FirebaseRepository
) : ListAdapter<ConversationItem, ConversationViewHolder>(ConversationDiffCallback)
{
    var actionMessageClick: (String) -> Unit = {}

    private fun cancelListeners(
        userListenerId: Int,
    )
    {
        repository.removeUserListener(userListenerId)
    }

    private val holders: MutableList<() -> Unit> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder =
            ConversationViewHolder.create(parent, ::cancelListeners, imageLoader).apply {
                holders.add(::cancelJobs)
            }

    @ExperimentalCoroutinesApi
    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) = holder.bind(
        conversationItem = getItem(position),
        actionConversationClick = actionMessageClick,
        userFlow = repository::getUser,
    )

    fun cancelScopes()
    {
        holders.forEach { cancelScope ->
            cancelScope()
        }
    }
}