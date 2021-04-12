package com.myniprojects.pixagram.utils.status

import com.myniprojects.pixagram.utils.Event
import com.myniprojects.pixagram.utils.Message

sealed class EventMessageStatus
{
    object Sleep : EventMessageStatus()
    object Loading : EventMessageStatus()
    data class Success(val eventMessage: Event<Message>) : EventMessageStatus()
    data class Failed(val eventMessage: Event<Message>) : EventMessageStatus()
}