package com.myniprojects.pixagram.utils.status

import com.myniprojects.pixagram.utils.Message

sealed class FirebaseStatus
{
    object Loading : FirebaseStatus()
    data class Success(val message: Message) : FirebaseStatus()
    data class Failed(val message: Message) : FirebaseStatus()
    object Sleep : FirebaseStatus()
}