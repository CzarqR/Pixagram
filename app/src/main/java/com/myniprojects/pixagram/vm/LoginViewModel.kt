package com.myniprojects.pixagram.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.ext.context
import com.myniprojects.pixagram.utils.ext.trim
import com.myniprojects.pixagram.utils.status.LoginRegisterStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class LoginViewModel @Inject constructor(
    application: Application,
    private val repository: FirebaseRepository
) : AndroidViewModel(application)
{
    val email: MutableLiveData<String> = MutableLiveData()
    val passwd: MutableLiveData<String> = MutableLiveData()
    val passwdConf: MutableLiveData<String> = MutableLiveData()
    val username: MutableLiveData<String> = MutableLiveData()
    val fullname: MutableLiveData<String> = MutableLiveData()

    private val _loginState = MutableStateFlow(LoginState.LOGIN)
    val loginState: StateFlow<LoginState> = _loginState

    val user = repository.loggedUser

    init
    {
        /**
         * TODO. Delete it later, now it is only to test faster
         */
        email.value = "czarekr98@gmail.com"
        passwd.value = "123456"
        passwdConf.value = "123456"
        username.value = "CzarqR"
        fullname.value = "Jan Kowalski"
    }

    fun changeState()
    {
        Timber.d("change state")
        _loginState.value =
                if (_loginState.value == LoginState.LOGIN) LoginState.REGISTRATION else LoginState.LOGIN
    }

    fun logOrRegister(): Flow<LoginRegisterStatus>
    {
        return if (_loginState.value == LoginState.LOGIN)
        {
            logIn()
        }
        else
        {
            register()
        }
    }

    private fun logIn(): Flow<LoginRegisterStatus>
    {
        email.trim()
        passwd.trim()

        return repository.loginUser(
            email = email.value,
            passwd = passwd.value
        )
    }

    private fun register(): Flow<LoginRegisterStatus>
    {
        email.trim()
        username.trim()
        passwd.trim()
        passwdConf.trim()
        fullname.trim()

        return repository.registerUser(
            email = email.value,
            passwd = passwd.value,
            passwdConf = passwdConf.value,
            fullname = fullname.value,
            username = username.value,
            context = context
        )
    }

    enum class LoginState
    {
        LOGIN,
        REGISTRATION
    }
}

