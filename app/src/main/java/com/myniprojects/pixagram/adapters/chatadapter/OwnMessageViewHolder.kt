package com.myniprojects.pixagram.adapters.chatadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.databinding.MessageOwnItemBinding

class OwnMessageViewHolder private constructor(
    private val binding: MessageOwnItemBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): OwnMessageViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = MessageOwnItemBinding.inflate(layoutInflater, parent, false)
            return OwnMessageViewHolder(
                binding
            )
        }
    }


    fun bind(
        message: MassageModel.OwnMessage,
    )
    {
        binding.txtBody.text = message.chatMessage.textContent
    }
}