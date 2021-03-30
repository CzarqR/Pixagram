package com.myniprojects.pixagram.vm

import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.status.DataStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: FirebaseRepository
) : ViewModelPostRecycler(repository)
{
    private val postsFromFollowingUsers = repository.postsToDisplay
    private val arePostsLoading = repository.arePostsLoading

    override val postToDisplay: Flow<DataStatus<Post>>
        get() = postsFromFollowingUsers.combine(arePostsLoading) { posts, loadingStatus ->
            if (loadingStatus)
            {
                DataStatus.Loading
            }
            else
            {
                // todo change this DataStatus should has list of PostWithId

                val hashMap = hashMapOf<String, Post>()

                posts.forEach {
                    hashMap[it.first] = it.second
                }

                DataStatus.Success(hashMap)
            }
        }

}