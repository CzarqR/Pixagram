package com.myniprojects.pixagram.vm

import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.DataStatus
import kotlinx.coroutines.flow.Flow

abstract class ViewModelPostRecycler(
    repository: FirebaseRepository
) : ViewModelPost(repository)
{
    abstract val postToDisplay: Flow<DataStatus<Post>>
}