package com.myniprojects.pixagram.utils.status

import com.myniprojects.pixagram.utils.Message

sealed class DataStatus<out T>
{
    object Loading : DataStatus<Nothing>()
    data class Success<T>(val data: HashMap<String, T>) : DataStatus<T>()
    data class Failed(val message: Message) : DataStatus<Nothing>()
}
