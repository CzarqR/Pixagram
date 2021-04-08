package com.myniprojects.pixagram.utils.status

import com.myniprojects.pixagram.utils.Message

sealed class GetStatus<out T>
{
    object Sleep : GetStatus<Nothing>()
    object Loading : GetStatus<Nothing>()
    data class Success<T>(val data: T) : GetStatus<T>()
    data class Failed(val message: Message) : GetStatus<Nothing>()
}
