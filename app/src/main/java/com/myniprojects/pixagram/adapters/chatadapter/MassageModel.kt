package com.myniprojects.pixagram.adapters.chatadapter

import androidx.constraintlayout.widget.ConstraintLayout
import com.myniprojects.pixagram.model.ChatMessage
import com.myniprojects.pixagram.model.User

sealed class MassageModel(val chatMessage: ChatMessage)
{
    data class OwnMessage(val message: ChatMessage, val type: MessageType) : MassageModel(message)

    data class OtherMessage(
        val message: ChatMessage,
        val type: MessageType,
        val user: User,
    ) : MassageModel(message)
}

enum class MessageType
{
    FIRST,
    MIDDLE,
    LAST,
    SINGLE
}

fun getTypeFromSenders(
    previous: String?,
    current: String,
    next: String?,
): MessageType
{
    when
    {
        previous == null ->
        {
            return when
            {
                next == null ->
                {
                    MessageType.SINGLE
                }
                current == next ->
                {
                    MessageType.FIRST
                }
                else ->
                {
                    MessageType.SINGLE
                }
            }
        }
        next == null ->
        {
            return when (previous)
            {
                current ->
                {
                    MessageType.LAST
                }
                else ->
                {
                    MessageType.SINGLE
                }
            }
        }
        else ->
        {
            return if (previous == current && current == next)
            {
                MessageType.MIDDLE
            }
            else if (previous != current && current == next)
            {
                MessageType.FIRST
            }
            else if (previous == current && current != next)
            {
                MessageType.LAST
            }
            else
            {
                MessageType.SINGLE
            }
        }
    }
}


fun ConstraintLayout.setMessageMargins(
    messageType: MessageType,
    messageDefMargin: Int,
    messageSeparator: Int,
)
{

    when (messageType)
    {
        MessageType.FIRST ->
        {
            setPadding(
                messageDefMargin,
                messageDefMargin,
                messageDefMargin,
                messageSeparator
            )
        }
        MessageType.MIDDLE ->
        {
            setPadding(
                messageDefMargin,
                messageDefMargin,
                messageDefMargin,
                messageDefMargin
            )
        }
        MessageType.LAST ->
        {
            setPadding(
                messageDefMargin,
                messageSeparator,
                messageDefMargin,
                messageDefMargin
            )
        }
        MessageType.SINGLE ->
        {
            setPadding(
                messageDefMargin,
                messageSeparator,
                messageDefMargin,
                messageSeparator
            )
        }
    }
}