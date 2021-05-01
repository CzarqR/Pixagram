package com.myniprojects.pixagram.adapters.chatadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.databinding.MessageItemBinding

class ChatMessageViewHolder private constructor(
    private val binding: MessageItemBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): ChatMessageViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = MessageItemBinding.inflate(layoutInflater, parent, false)
            return ChatMessageViewHolder(
                binding
            )
        }
    }


    fun bind(
        chatMessageData: ChatMessageData,
        glide: RequestManager
    )
    {
        binding.txtBody.text = chatMessageData.chatMessage.textContent

        glide
            .load(chatMessageData.user.imageUrl)
            .into(binding.imgAvatar)
    }
}