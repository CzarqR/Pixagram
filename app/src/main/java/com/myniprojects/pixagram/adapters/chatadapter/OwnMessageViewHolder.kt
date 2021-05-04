package com.myniprojects.pixagram.adapters.chatadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.shape.CornerFamily
import com.myniprojects.pixagram.databinding.MessageOwnItemBinding

class OwnMessageViewHolder private constructor(
    private val binding: MessageOwnItemBinding
) : MessageModelViewHolder<MessageOwnItemBinding>(binding)
{
    companion object
    {
        fun create(parent: ViewGroup): OwnMessageViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = MessageOwnItemBinding.inflate(layoutInflater, parent, false)
            return OwnMessageViewHolder(
                binding
            ).apply {
                initViewHolder()
            }
        }
    }

    fun bind(
        message: MassageModel.OwnMessage,
        messageClickListener: MessageClickListener
    )
    {
        bindRoutine(message, messageClickListener)

        with(binding)
        {

            // region card styling

            val b = cardView.shapeAppearanceModel.toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)

            when (message.type)
            {
                MessageType.FIRST ->
                {
                    b.setTopRightCornerSize(0f)
                }
                MessageType.MIDDLE ->
                {
                    b.setTopRightCornerSize(0f)
                    b.setBottomRightCornerSize(0f)
                }
                MessageType.LAST ->
                {
                    b.setBottomRightCornerSize(0f)
                }
                MessageType.SINGLE ->
                {

                }
            }

            cardView.shapeAppearanceModel = b.build()

            // endregion
        }

    }
}