package com.myniprojects.pixagram.vm

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.ext.context
import com.myniprojects.pixagram.utils.ext.getFileExt
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
    private val repository: FirebaseRepository,
    application: Application
) : AndroidViewModel(application)
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

    private val _newImageUri: MutableStateFlow<Uri?> = MutableStateFlow(null)
    val newImageUri = _newImageUri.asStateFlow()

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
                repository.updateUser(
                    user = _newUserData.value,
                    uri = _newImageUri.value,
                    fileExtension = _newImageUri.value?.getFileExt(context.contentResolver)
                ).collectLatest {
                    _editStatus.value = it
                    if (it is EventMessageStatus.Success)
                    {
                        _baseUser.value = _newUserData.value
                        _newImageUri.value = null
                        _isAnythingChanged.value = _newUserData.value != _baseUser.value
                    }
                }
            }
        }
    }

    fun cancel()
    {
        _newImageUri.value = null

        _baseUser.value?.let {
            _newUserData.value = it
        }

        _isAnythingChanged.value = false
    }

    fun setImage(uri: Uri)
    {
        _newImageUri.value = uri
        _newUserData.value = _newUserData.value.copy(imageUrl = uri.toString())
        _isAnythingChanged.value = true
    }
}
