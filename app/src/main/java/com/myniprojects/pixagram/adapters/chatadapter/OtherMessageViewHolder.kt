package com.myniprojects.pixagram.adapters.chatadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.databinding.MessageOtherItemBinding

class OtherMessageViewHolder private constructor(
    private val binding: MessageOtherItemBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): OtherMessageViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = MessageOtherItemBinding.inflate(layoutInflater, parent, false)
            return OtherMessageViewHolder(
                binding
            )
        }
    }


    fun bind(
        message: MassageModel.OtherMessage,
        glide: RequestManager
    )
    {
        binding.txtBody.text = message.chatMessage.textContent

        glide
            .load(message.user.imageUrl)
            .into(binding.imgAvatar)
    }
}