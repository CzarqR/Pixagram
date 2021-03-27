package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.DataStatus
import com.myniprojects.pixagram.utils.status.GetStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TagViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    private val _tag: MutableStateFlow<GetStatus<Tag>> = MutableStateFlow(GetStatus.Loading)
    val tag: StateFlow<GetStatus<Tag>> = _tag

    var posts: Flow<DataStatus<Post>> = flowOf(DataStatus.Loading)


    @ExperimentalCoroutinesApi
    fun initTag(tag: Tag)
    {

        val t = tag.copy(title = tag.title.toLowerCase(Locale.ENGLISH))

        /**
         * When tag count is equal 1 it means that tag has to be loaded from db
         * Happens when fragment is opened from clicking hashtag in post description
         */
        if (t.count == -1L)
        {
            viewModelScope.launch {
                repository.getTag(t.title).collectLatest {
                    _tag.value = it
                }
            }
        }
        else
        {
            _tag.value = GetStatus.Success(t)
        }

        posts = repository.getAllPostsFromTag(t.title)
    }

}