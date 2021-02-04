package com.myniprojects.pixagram.utils.status

import com.myniprojects.pixagram.utils.Message

sealed class LoginRegisterStatus
{
    object Loading : LoginRegisterStatus()
    data class Success(val message: Message) : LoginRegisterStatus()
    data class Failed(val message: Message) : LoginRegisterStatus()
}