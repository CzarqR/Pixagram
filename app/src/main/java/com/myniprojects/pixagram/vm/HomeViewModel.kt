package com.myniprojects.pixagram.vm

import com.myniprojects.pixagram.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: FirebaseRepository
) : ViewModelPost(repository)
{
    val postsFromFollowingUsers = repository.postsToDisplay
    val arePostsLoading = repository.arePostsLoading

}