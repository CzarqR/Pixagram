package com.myniprojects.pixagram.vm.utils

import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.GetStatus
import kotlinx.coroutines.flow.Flow

abstract class ViewModelPostRecycler(
    repository: FirebaseRepository
) : ViewModelPost(repository)
{
    /**
     * this has not to be sorted
     * list is sorted descending by time in fragment
     */
    abstract val postToDisplay: Flow<GetStatus<List<PostWithId>>>

    /**
     * if child override this in error state will be button
     * `Try Again` and after clicking this function will be executed
     */
    open val tryAgain: (() -> Unit)? = null
}