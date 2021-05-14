package com.myniprojects.pixagram.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.EventMessageStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel()
{
    val newEmail: MutableLiveData<String> = MutableLiveData()
    val passwd: MutableLiveData<String> = MutableLiveData()

    private val _basePost: MutableStateFlow<Post> = MutableStateFlow(Post())
    val basePost = _basePost.asStateFlow()

    private val _updatedPost: MutableStateFlow<Post> = MutableStateFlow(Post())
    val updatedPost = _updatedPost.asStateFlow()

    private val _isAnythingChanged = MutableStateFlow(false)
    val isAnythingChanged = _isAnythingChanged.asStateFlow()

    fun updateDesc(desc: String)
    {
        _updatedPost.value = _updatedPost.value.copy(desc = desc)
        _isAnythingChanged.value = _updatedPost.value != _basePost.value
    }


    private lateinit var postId: String
    fun initPost(post: PostWithId)
    {
        _basePost.value = post.second
        _updatedPost.value = post.second
        postId = post.first
    }

    @ExperimentalCoroutinesApi
    fun save(
        newHashtags: List<String>,
        newMentions: List<String>,
        oldHashtags: List<String>,
        oldMentions: List<String>,
    ): Flow<EventMessageStatus>
    {
        _basePost.value = _updatedPost.value
        _isAnythingChanged.value = _updatedPost.value != _basePost.value

        return editPost(
            postId,
            _updatedPost.value.desc,
            newHashtags,
            newMentions,
            oldHashtags,
            oldMentions
        )
    }

    @ExperimentalCoroutinesApi
    fun editPost(
        postId: String,
        newDesc: String,
        newHashtags: List<String>,
        newMentions: List<String>,
        oldHashtags: List<String>,
        oldMentions: List<String>,
    ) = repository.editPost(postId, newDesc, newHashtags, newMentions, oldHashtags, oldMentions)
}