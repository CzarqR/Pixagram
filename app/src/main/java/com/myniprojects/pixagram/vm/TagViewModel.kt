package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.DataStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class TagViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    private val _tag = MutableStateFlow(Tag())
    val tag: StateFlow<Tag> = _tag

    var posts: Flow<DataStatus<Post>> = flowOf(DataStatus.Loading)


    @ExperimentalCoroutinesApi
    fun initTag(tag: Tag)
    {
        _tag.value = tag

        posts = repository.getAllPostsFromTag(tag.title)
    }

}