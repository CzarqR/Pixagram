package com.myniprojects.pixagram.adapters.chatadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.material.shape.CornerFamily
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.MessageOtherItemBinding
import com.myniprojects.pixagram.utils.ext.context
import com.myniprojects.pixagram.utils.ext.isOnlyEmoji
import com.myniprojects.pixagram.utils.ext.px

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
        message: MassageModel.OtherMessage,
        glide: RequestManager
    )
    {
        with(binding)
        {
            txtBody.text = message.chatMessage.textContent

            txtBody.textSize =
                    if (message.message.textContent?.isOnlyEmoji == true)
                        emojiFontSize
                    else normalFontSize

            glide
                .load(message.user.imageUrl)
                .into(imgAvatar)


            // region card styling

            val r = binding.context.resources.getDimension(R.dimen.message_corner_radius)

            val b = cardView.shapeAppearanceModel.toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, r)

            when (message.type)
            {
                MessageType.FIRST ->
                {
                    b.setTopLeftCornerSize(0f)
                    b.setBottomLeftCornerSize(0f)

                    (cardView.layoutParams as ConstraintLayout.LayoutParams).setMargins(
                        messageDefMargin,
                        messageDefMargin,
                        messageDefMargin,
                        messageSeparator
                    )
                }
                MessageType.MIDDLE ->
                {
                    b.setTopLeftCornerSize(0f)
                    b.setBottomLeftCornerSize(0f)

                    (cardView.layoutParams as ConstraintLayout.LayoutParams).setMargins(
                        messageDefMargin,
                        messageDefMargin,
                        messageDefMargin,
                        messageDefMargin
                    )
                }
                MessageType.LAST ->
                {
                    b.setBottomLeftCornerSize(0f)

                    (cardView.layoutParams as ConstraintLayout.LayoutParams).setMargins(
                        messageDefMargin,
                        messageSeparator,
                        messageDefMargin,
                        messageDefMargin
                    )
                }
                MessageType.SINGLE ->
                {
                    b.setBottomLeftCornerSize(0f)

                    (cardView.layoutParams as ConstraintLayout.LayoutParams).setMargins(
                        messageDefMargin,
                        messageSeparator,
                        messageDefMargin,
                        messageSeparator
                    )
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