package com.myniprojects.pixagram.utils.ext

import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.databinding.StateRecyclerBinding
import com.myniprojects.pixagram.utils.status.GetStatus

data class StateData(
    @StringRes val emptyStateText: Int,
    @DrawableRes val emptyStateIcon: Int,
    @DimenRes val bottomRecyclerPadding: Int = R.dimen.bottom_place_holder_home
)

enum class RecyclerState
{
    LOADING,
    EMPTY,
    ERROR,
    SUCCESS
}

private fun StateRecyclerBinding.setVisibility(state: RecyclerState)
{
    proBarLoadingPosts.isVisible = state == RecyclerState.LOADING
    linLayEmptyState.isVisible = state == RecyclerState.EMPTY
    linLayErrorState.isVisible = state == RecyclerState.ERROR
    rvPosts.isVisible = state == RecyclerState.SUCCESS
}

fun StateRecyclerBinding.setState(
    status: GetStatus<List<PostWithId>>,
    postAdapter: PostAdapter?
)
{
    when (status)
    {
        GetStatus.Sleep -> Unit
        GetStatus.Loading ->
        {
            setVisibility(RecyclerState.LOADING)
        }
        is GetStatus.Failed ->
        {
            setVisibility(RecyclerState.ERROR)
            txtErrorState.text = status.message.getFormattedMessage(context)
        }
        is GetStatus.Success ->
        {
            if (status.data.isEmpty())
            {
                setVisibility(RecyclerState.EMPTY)
            }
            else
            {
                setVisibility(RecyclerState.SUCCESS)
            }
            postAdapter?.submitList(status.data.sortedByDescending { post ->
                post.second.time
            })
        }
    }
}

fun StateRecyclerBinding.setupView(
    stateData: StateData,
    tryAgain: (() -> Unit)? = null
)
{
    /**
     * Set button TryAgain visibility based on function in VM
     */
    butTryAgain.isVisible = tryAgain != null

    butTryAgain.setOnClickListener {
        tryAgain?.invoke()
    }

    txtEmptyState.text = context.getString(stateData.emptyStateText)
    imgIconEmptyState.setImageResource(stateData.emptyStateIcon)

    rvPosts.updatePadding(bottom = context.resources.getDimensionPixelOffset(stateData.bottomRecyclerPadding))
}