package com.myniprojects.pixagram.utils

import androidx.annotation.StringRes

sealed class LoginRegisterStatus
{
    object Loading : LoginRegisterStatus()
    data class Success(val message: Message) : LoginRegisterStatus()
    data class Failed(val message: Message) : LoginRegisterStatus()
}

val <T> T.exhaustive: T
    get() = this

