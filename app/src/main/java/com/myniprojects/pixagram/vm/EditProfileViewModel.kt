package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.EventMessageStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    private val _newUserData: MutableStateFlow<User> = MutableStateFlow(User())
    val newUserData = _newUserData.asStateFlow()

    private var _baseUser: MutableStateFlow<User?> = MutableStateFlow(null)
    var baseUser = _baseUser.asStateFlow()

    private val _isAnythingChanged = MutableStateFlow(false)
    val isAnythingChanged = _isAnythingChanged.asStateFlow()

    private val _editStatus: MutableStateFlow<EventMessageStatus> = MutableStateFlow(
        EventMessageStatus.Sleep
    )
    val editStatus = _editStatus.asStateFlow()

    init
    {
        viewModelScope.launch {
            repository.loggedUserData.collectLatest {
                if (_baseUser.value == null)
                {
                    _baseUser.value = it
                    _newUserData.value = it
                }
            }
        }
    }

    fun updateFullname(fullname: String)
    {
        _newUserData.value = _newUserData.value.copy(fullName = fullname)
        _isAnythingChanged.value = _newUserData.value != _baseUser.value
    }

    fun updateBio(bio: String)
    {
        _newUserData.value = _newUserData.value.copy(bio = bio)
        _isAnythingChanged.value = _newUserData.value != _baseUser.value
    }

    @ExperimentalCoroutinesApi
    fun save()
    {
        if (_editStatus.value != EventMessageStatus.Loading)
        {
            viewModelScope.launch {
                repository.updateUser(_newUserData.value).collectLatest {
                    _editStatus.value = it
                    if (it is EventMessageStatus.Success)
                    {
                        _baseUser.value = _newUserData.value
                        _isAnythingChanged.value = _newUserData.value != _baseUser.value
                    }
                }
            }
        }
    }

    fun cancel()
    {
        _baseUser.value?.let {
            _newUserData.value = it
        }
    }
}
