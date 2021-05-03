package com.myniprojects.pixagram.adapters.chatadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.CornerFamily
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.MessageOwnItemBinding
import com.myniprojects.pixagram.utils.ext.context
import com.myniprojects.pixagram.utils.ext.isOnlyEmoji
import com.myniprojects.pixagram.utils.ext.px

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
            ).apply {
                radius = binding.context.resources.getDimension(R.dimen.message_corner_radius)
                messageDefMargin = (binding.context.resources.getInteger(R.integer.message_default_margin)).px
                messageSeparator = (binding.context.resources.getInteger(R.integer.message_separator)).px
            }
        }
    }

    private var radius: Float = 0F
    private var messageDefMargin: Int = 0
    private var messageSeparator: Int = 0

    fun bind(
        message: MassageModel.OwnMessage,
    )
    {
        with(binding)
        {
            txtBody.text = message.chatMessage.textContent

            txtBody.textSize =
                    if (message.message.textContent?.isOnlyEmoji == true)
                        emojiFontSize
                    else normalFontSize


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

            clRoot.setMessageMargins(message.type, messageDefMargin, messageSeparator)

            // endregion
        }

    }
}