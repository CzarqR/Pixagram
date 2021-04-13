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
class ChangePasswdViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    val currPasswd: MutableLiveData<String> = MutableLiveData()
    val newPasswd: MutableLiveData<String> = MutableLiveData()
    val confirmPasswd: MutableLiveData<String> = MutableLiveData()

    private val _updateStatus: MutableStateFlow<EventMessageStatus> = MutableStateFlow(
        EventMessageStatus.Sleep
    )
    val updateStatus = _updateStatus.asStateFlow()

    @ExperimentalCoroutinesApi
    fun changePasswd()
    {
        val curr = currPasswd.value
        val new = newPasswd.value
        val conf = confirmPasswd.value
        if (curr != null && new != null && conf != null)
        {
            viewModelScope.launch {
                repository.changePasswd(curr, new, conf).collectLatest {
                    _updateStatus.value = it
                    if (it is EventMessageStatus.Success)
                    {
                        currPasswd.value = ""
                        newPasswd.value = ""
                        confirmPasswd.value = ""
                    }
                }
            }
        }
    }
}