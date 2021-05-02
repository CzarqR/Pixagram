package com.myniprojects.pixagram.adapters.chatadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.CornerFamily
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.MessageOwnItemBinding
import com.myniprojects.pixagram.utils.ext.context

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

        with(binding)
        {
            txtBody.text = message.chatMessage.textContent

            // region card styling

            val r = binding.context.resources.getDimension(R.dimen.message_corner_radius)

            val b = cardView.shapeAppearanceModel.toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, r)

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