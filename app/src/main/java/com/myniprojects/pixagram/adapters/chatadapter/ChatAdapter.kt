package com.myniprojects.pixagram.adapters.chatadapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.R
import javax.inject.Inject

class ChatAdapter @Inject constructor(
    private val glide: RequestManager
) : ListAdapter<MassageModel, RecyclerView.ViewHolder>(ChatMessageDiffCallback)
{

    /**
     * [messageClickListener] has to be initialized before
     * [ChatAdapter] is assigned to [RecyclerView]
     */
    lateinit var messageClickListener: MessageClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType)
            {
                R.layout.message_other_item -> OtherMessageViewHolder.create(parent)
                R.layout.message_own_item -> OwnMessageViewHolder.create(parent)
                else -> throw IllegalArgumentException("Layout cannot be displayed in RecyclerView")
            }

    override fun getItemViewType(position: Int): Int =
            when (getItem(position))
            {
                is MassageModel.OwnMessage -> R.layout.message_own_item
                is MassageModel.OtherMessage -> R.layout.message_other_item
                null -> throw UnsupportedOperationException("Unknown view")
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        getItem(position).let {
            when (it)
            {
                is MassageModel.OtherMessage -> (holder as OtherMessageViewHolder).bind(
                    it,
                    messageClickListener,
                    glide
                )
                is MassageModel.OwnMessage -> (holder as OwnMessageViewHolder).bind(
                    it,
                    messageClickListener
                )
            }
        }
    }


}