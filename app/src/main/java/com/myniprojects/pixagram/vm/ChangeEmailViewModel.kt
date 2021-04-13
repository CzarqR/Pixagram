package com.myniprojects.pixagram.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class ChangeEmailViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    val newEmail: MutableLiveData<String> = MutableLiveData()
    val passwd: MutableLiveData<String> = MutableLiveData()

    private val _updateStatus: MutableStateFlow<EventMessageStatus> = MutableStateFlow(
        EventMessageStatus.Sleep
    )
    val updateStatus = _updateStatus.asStateFlow()

    @ExperimentalCoroutinesApi
    fun changeEmail()
    {
        val ne = newEmail.value
        val p = passwd.value
        if (ne != null && p != null)
        {
            viewModelScope.launch {
                repository.changeEmail(p, ne).collectLatest {
                    _updateStatus.value = it
                }
            }
        }
    }
}