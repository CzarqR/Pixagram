package com.myniprojects.pixagram.vm

import androidx.annotation.StringRes
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.utils.Constants.PASSWD_MIN_LENGTH
import com.myniprojects.pixagram.utils.Constants.USERNAME_MIN_LENGTH
import com.myniprojects.pixagram.utils.DatabaseFields
import com.myniprojects.pixagram.utils.Event
import com.myniprojects.pixagram.utils.trim
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class LoginViewModel @ViewModelInject constructor() : ViewModel()
{
    private val dbRootRef = Firebase.database.reference
    private val auth = Firebase.auth

    val email: MutableLiveData<String> = MutableLiveData()
    val passwd: MutableLiveData<String> = MutableLiveData()
    val passwdConf: MutableLiveData<String> = MutableLiveData()
    val username: MutableLiveData<String> = MutableLiveData()

    private val _loginState = MutableStateFlow(LoginState.LOGIN)
    val loginState: StateFlow<LoginState> = _loginState

    private val _message = MutableStateFlow<Event<Int>?>(null)
    val message: StateFlow<Event<Int>?> = _message

    private val _user = MutableStateFlow(Event(auth.currentUser))
    val user: StateFlow<Event<FirebaseUser?>> = _user

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init
    {
        auth.addAuthStateListener {
            Timber.d("new user ${it.currentUser}")
            _user.value = Event(it.currentUser)
        }
    }

    fun changeState()
    {
        Timber.d("change state")
        _loginState.value =
                if (_loginState.value == LoginState.LOGIN) LoginState.REGISTRATION else LoginState.LOGIN
    }

    fun logOrRegister()
    {
        if (_loginState.value == LoginState.LOGIN)
        {
            logIn()
        }
        else
        {
            register()
        }?.let {
            _message.value = Event(it)
        }

    }

    @StringRes
    private fun logIn(): Int?
    {
        email.trim()
        passwd.trim()

        val e = email.value
        val p = passwd.value

        return if (e.isNullOrBlank()) // empty email
        {
            R.string.empty_email
        }
        else if (p.isNullOrBlank() || p.length < PASSWD_MIN_LENGTH) // too short passwd
        {
            R.string.invalid_password
        }
        else
        {
            _loading.value = true
            auth.signInWithEmailAndPassword(e, p)
                .addOnCompleteListener {
                    _loading.value = false
                }
                .addOnFailureListener {
                    _message.value = Event(R.string.wrong_email_or_passwd)
                }
            null
        }
    }

    @StringRes
    private fun register(): Int?
    {
        email.trim()
        username.trim()
        passwd.trim()
        passwdConf.trim()

        val e = email.value
        val u = username.value
        val p = passwd.value
        val pc = passwdConf.value

        if (e.isNullOrBlank()) // empty email
        {
            return R.string.empty_email
        }
        else if (u.isNullOrBlank() || u.length < USERNAME_MIN_LENGTH) // too short username
        {
            return R.string.invalid_username
        }
        else if (p.isNullOrBlank() || p.length < PASSWD_MIN_LENGTH) // too short passwd
        {
            return R.string.invalid_password
        }
        else if (p != pc) //passwords are different
        {
            return R.string.diff_passwd
        }
        else // can register
        {
            _loading.value = true

            auth.createUserWithEmailAndPassword(e, p)
                .addOnSuccessListener { authResult ->
                    val newUser = authResult.user

                    if (newUser != null)
                    {
                        val userData = hashMapOf(
                            DatabaseFields.USERS_FIELD_EMAIL to e,
                            DatabaseFields.USERS_FIELD_USERNAME to u,
                            DatabaseFields.USERS_FIELD_ID to newUser.uid,
                            DatabaseFields.USERS_FIELD_BIO to DatabaseFields.USERS_DEF_FIELD_BIO,
                            DatabaseFields.USERS_FIELD_IMAGE to DatabaseFields.USERS_DEF_FIELD_IMAGE,
                        )

                        dbRootRef.child(DatabaseFields.USERS_NAME)
                            .child(newUser.uid)
                            .setValue(userData)
                            .addOnSuccessListener {
                                _message.value = Event(R.string.user_created)
                            }
                            .addOnFailureListener {
                                _message.value = Event(R.string.cannot_save_user_into_db)
                            }
                            .addOnCompleteListener {
                                _loading.value = false
                            }
                    }
                }
                .addOnFailureListener {
                    Timber.d(it)
                    _message.value = Event(R.string.cannot_create_user)
                    _loading.value = false
                }

            return null
        }
    }

    enum class LoginState
    {
        LOGIN,
        REGISTRATION
    }
}

