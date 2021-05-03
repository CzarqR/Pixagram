package com.myniprojects.pixagram.adapters.chatadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.material.shape.CornerFamily
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.MessageOtherItemBinding
import com.myniprojects.pixagram.utils.ext.context
import com.myniprojects.pixagram.utils.ext.getDateTimeFormatFromMillis
import com.myniprojects.pixagram.utils.ext.isOnlyEmoji
import com.myniprojects.pixagram.utils.ext.px
import timber.log.Timber

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

                binding.cardView.setOnClickListener {
                    isTimeShown = !isTimeShown
                }

                binding.cardView.setOnLongClickListener {
                    return@setOnLongClickListener showPopupMenu(it)
                }
            }
        }
    }

    private var radius: Float = 0F
    private var messageDefMargin: Int = 0
    private var messageSeparator: Int = 0

    private lateinit var messageClickListener: MessageClickListener
    private lateinit var message: MassageModel.OtherMessage

    private var isTimeShown: Boolean = false
        set(value)
        {
            Timber.d("Set $value")
            field = value
            binding.txtTime.isVisible = value
        }

    fun bind(
        message: MassageModel.OtherMessage,
        messageClickListener: MessageClickListener,
        glide: RequestManager
    )
    {
        this.messageClickListener = messageClickListener
        this.message = message
        isTimeShown = false
        binding.txtTime.text = message.message.time.getDateTimeFormatFromMillis()

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

            clRoot.setMessageMargins(message.type, messageDefMargin, messageSeparator)

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

    private fun showPopupMenu(view: View): Boolean
    {
        val popupMenu = PopupMenu(view.context, view)

        popupMenu.inflate(R.menu.message_dropdown)

        popupMenu.setOnMenuItemClickListener { menuItem ->

            return@setOnMenuItemClickListener when (menuItem.itemId)
            {
                R.id.mi_copy ->
                {
                    messageClickListener.copyText(message.message)
                    true
                }
                R.id.mi_delete ->
                {
                    messageClickListener.deleteMessage(message.message)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()

        return true
    }
}