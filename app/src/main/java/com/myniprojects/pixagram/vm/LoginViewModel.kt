package com.myniprojects.pixagram.vm

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.utils.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class LoginViewModel : ViewModel()
{
    private val dbRootRef = Firebase.database.reference

    val email: MutableLiveData<String> = MutableLiveData()
    val passwd: MutableLiveData<String> = MutableLiveData()
    val passwdConf: MutableLiveData<String> = MutableLiveData()
    val username: MutableLiveData<String> = MutableLiveData()

    private val _loginState = MutableStateFlow(LoginState.LOGIN)
    val loginState: StateFlow<LoginState> = _loginState

    private val _message = MutableStateFlow<Event<Int>?>(null)
    val message: StateFlow<Event<Int>?> = _message

    fun changeState()
    {
        Timber.d("change state")
        _loginState.value =
                if (_loginState.value == LoginState.LOGIN) LoginState.REGISTRATION else LoginState.LOGIN
    }

    fun logOrRegister()
    {
        _message.value = Event(
            if (_loginState.value == LoginState.LOGIN)
            {
                logIn()
            }
            else
            {
                register()
            }
        )
    }

    @StringRes
    private fun logIn(): Int
    {
        return R.string.log_in
    }

    @StringRes
    private fun register(): Int
    {
        return R.string.register
    }

    enum class LoginState
    {
        LOGIN,
        REGISTRATION
    }
}

