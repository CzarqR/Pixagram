package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel : ViewModel()
{

    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user

    fun initUser(user: User)
    {
        _user.value = user
    }
}