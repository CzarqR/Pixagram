package com.myniprojects.pixagram.adapters.chatadapter

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.utils.ext.context
import com.myniprojects.pixagram.utils.ext.getDateTimeFormatFromMillis
import com.myniprojects.pixagram.utils.ext.isOnlyEmoji
import com.myniprojects.pixagram.utils.ext.px

const val normalFontSize: Float = 14F
const val emojiFontSize: Float = 28F

abstract class MessageModelViewHolder<T>(
    private val binding: T
) : RecyclerView.ViewHolder(binding.root) where T : ViewBinding
{
    private lateinit var cardView: MaterialCardView
    private lateinit var txtTime: MaterialTextView
    private lateinit var txtBody: MaterialTextView

    protected fun initViewHolder()
    {
        radius = binding.context.resources.getDimension(R.dimen.message_corner_radius)
        messageDefMargin = (binding.context.resources.getInteger(R.integer.message_default_margin)).px
        messageSeparator = (binding.context.resources.getInteger(R.integer.message_separator)).px

        cardView = binding.root.findViewById(R.id.cardView)
        txtTime = binding.root.findViewById(R.id.txtTime)
        txtBody = binding.root.findViewById(R.id.txtBody)

        cardView.setOnClickListener {
            isTimeShown = !isTimeShown
        }

        cardView.setOnLongClickListener {
            return@setOnLongClickListener showPopupMenu(it)
        }
    }

    protected var radius: Float = 0F
    protected var messageDefMargin: Int = 0
    protected var messageSeparator: Int = 0

    private lateinit var messageClickListener: MessageClickListener
    protected lateinit var message: MassageModel

    private var isTimeShown: Boolean = false
        set(value)
        {
            field = value
            txtTime.isVisible = value
        }

    protected fun bindRoutine(
        message: MassageModel,
        messageClickListener: MessageClickListener,
    )
    {
        this.messageClickListener = messageClickListener
        this.message = message
        isTimeShown = false
        txtTime.text = message.chatMessage.time.getDateTimeFormatFromMillis()
        txtBody.text = message.chatMessage.textContent

        txtBody.textSize =
                if (message.chatMessage.textContent?.isOnlyEmoji == true)
                    emojiFontSize
                else normalFontSize

        binding.root.setMessageMargins(message.type, messageDefMargin, messageSeparator)
    }

    fun showPopupMenu(view: View): Boolean
    {
        val popupMenu = PopupMenu(view.context, view)

        popupMenu.inflate(R.menu.message_dropdown)

        popupMenu.setOnMenuItemClickListener { menuItem ->

            return@setOnMenuItemClickListener when (menuItem.itemId)
            {
                R.id.mi_copy ->
                {
                    messageClickListener.copyText(message.chatMessage)
                    true
                }
                R.id.mi_delete ->
                {
                    messageClickListener.deleteMessage(message.chatMessage)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()

        return true
    }
}