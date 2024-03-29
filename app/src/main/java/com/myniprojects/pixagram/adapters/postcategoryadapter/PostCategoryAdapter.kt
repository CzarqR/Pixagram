package com.myniprojects.pixagram.adapters.postcategoryadapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.utils.ext.StateData
import com.myniprojects.pixagram.utils.status.GetStatus
import kotlinx.coroutines.flow.Flow

class PostCategoryAdapter(
    private val stateRecyclerData: List<StateRecyclerData>
) : RecyclerView.Adapter<RecyclerStateViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerStateViewHolder =
            RecyclerStateViewHolder.create(parent)

    override fun onBindViewHolder(holder: RecyclerStateViewHolder, position: Int) =
            holder.bind(stateRecyclerData[position])

    override fun getItemCount(): Int = stateRecyclerData.size

}

data class StateRecyclerData(
    val postsToDisplay: Flow<GetStatus<List<PostWithId>>>,
    val postAdapter: PostAdapter,
    val stateData: StateData,
    val tryAgain: (() -> Unit)? = null
)