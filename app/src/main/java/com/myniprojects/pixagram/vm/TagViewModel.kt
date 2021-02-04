package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.model.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class TagViewModel @Inject constructor() : ViewModel()
{
    private val _tag = MutableStateFlow(Tag())
    val tag: StateFlow<Tag> = _tag

    fun initTag(tag: Tag)
    {
        _tag.value = tag
    }
}