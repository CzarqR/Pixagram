package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TagViewModel : ViewModel()
{
    private val _tag = MutableStateFlow(Tag())
    val tag: StateFlow<Tag> = _tag

    fun initTag(tag: Tag)
    {
        _tag.value = tag
    }
}