package com.myniprojects.pixagram.adapters.chatadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.google.android.material.shape.CornerFamily
import com.myniprojects.pixagram.databinding.MessageOtherItemBinding

class OtherMessageViewHolder private constructor(
    private val binding: MessageOtherItemBinding
) : MessageModelViewHolder<MessageOtherItemBinding>(binding)
{
    companion object
    {
        fun create(parent: ViewGroup): OtherMessageViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = MessageOtherItemBinding.inflate(layoutInflater, parent, false)
            return OtherMessageViewHolder(
                binding
            ).apply {
                initViewHolder()
            }
        }
    }


    fun bind(
        message: MassageModel.OtherMessage,
        messageClickListener: MessageClickListener,
        glide: RequestManager
    )
    {
        bindRoutine(message, messageClickListener)

        with(binding)
        {
            glide
                .load(message.user.imageUrl)
                .into(imgAvatar)

            // region card styling

            val b = cardView.shapeAppearanceModel.toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)

            when (message.type)
            {
                MessageType.FIRST ->
                {
                    b.setTopLeftCornerSize(0f)
                    b.setBottomLeftCornerSize(0f)
                }
                MessageType.MIDDLE ->
                {
                    b.setTopLeftCornerSize(0f)
                    b.setBottomLeftCornerSize(0f)
                }
                MessageType.LAST ->
                {
                    b.setBottomLeftCornerSize(0f)
                }
                MessageType.SINGLE ->
                {
                    b.setBottomLeftCornerSize(0f)
                }
            }

            cardView.shapeAppearanceModel = b.build()

            // endregion

            if (message.type == MessageType.FIRST || message.type == MessageType.SINGLE)
            {
                imgAvatar.visibility = View.VISIBLE
            }
            else
            {
                imgAvatar.visibility = View.INVISIBLE
            }
        }
    }
}