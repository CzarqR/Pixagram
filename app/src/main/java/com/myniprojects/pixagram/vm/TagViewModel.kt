package com.myniprojects.pixagram.vm

import androidx.lifecycle.viewModelScope
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.ext.normalize
import com.myniprojects.pixagram.utils.status.DataStatus
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.vm.utils.ViewModelPostRecycler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModelPostRecycler(repository)
{
    private val _tag: MutableStateFlow<GetStatus<Tag>> = MutableStateFlow(GetStatus.Loading)
    val tag: StateFlow<GetStatus<Tag>> = _tag

    @ExperimentalCoroutinesApi
    fun initTag(tag: Tag)
    {

        val t = tag.copy(title = tag.title.normalize())

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

        viewModelScope.launch {
            repository.getAllPostsFromTag(t.title).collectLatest {
                _postToDisplay.value = it
            }
        }

    }

    private val _postToDisplay: MutableStateFlow<GetStatus<List<PostWithId>>> = MutableStateFlow(
        GetStatus.Loading
    )
    override val postToDisplay = _postToDisplay.asStateFlow()

}