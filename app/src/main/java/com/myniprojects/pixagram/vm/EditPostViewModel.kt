package com.myniprojects.pixagram.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
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

    fun save()
    {
        Timber.d("Saving post with id: [$postId]. New desc: `${_updatedPost.value.desc}`")
    }
}