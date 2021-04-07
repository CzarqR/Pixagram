package com.myniprojects.pixagram.vm

import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.vm.utils.ViewModelStateRecycler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: FirebaseRepository
) : ViewModelStateRecycler(repository)
{
    private val postsFromFollowingUsers = repository.postsToDisplay
    private val arePostsLoading = repository.arePostsLoading

    /**
     * TODO
     * Posts in home feed doesn't emit Failed state
     * change this in [FirebaseRepository]
     */
    override val postToDisplay: Flow<GetStatus<List<PostWithId>>>
        get() = postsFromFollowingUsers.combine(arePostsLoading) { posts, loadingStatus ->

            if (loadingStatus)
            {
                GetStatus.Loading
            }
            else
            {
                GetStatus.Success(posts.sortedByDescending {
                    it.second.time
                })
            }
        }

    override val tryAgain: (() -> Unit)
        get() = {
            Timber.d("Try again")
        }
}