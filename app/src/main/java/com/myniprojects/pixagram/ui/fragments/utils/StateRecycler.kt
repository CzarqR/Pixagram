package com.myniprojects.pixagram.ui.fragments.utils

import android.os.Bundle
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.databinding.StateRecyclerBinding
import com.myniprojects.pixagram.utils.ext.context
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.vm.utils.ViewModelStateRecycler
import kotlinx.coroutines.flow.collectLatest

class StateRecycler : Fragment(R.layout.state_recycler)
{
    private lateinit var viewModel: ViewModelStateRecycler
    private var postAdapter: PostAdapter? = null
    private lateinit var stateData: StateData

    val binding by viewBinding(StateRecyclerBinding::bind)

    fun initView(
        viewModel: ViewModelStateRecycler,
        postAdapter: PostAdapter,
        stateData: StateData
    )
    {
        this.viewModel = viewModel
        this.postAdapter = postAdapter
        this.stateData = stateData
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        /**
         * when post adapter is not equal to null
         * it means that fragment fields are initialized
         */
        if (postAdapter != null)
        {
            setupView()
            setupRecycler()
        }
    }

    /**
     * When View is destroyed adapter should cancel scope in every ViewHolder
     */
    override fun onDestroyView()
    {
        super.onDestroyView()
        postAdapter?.cancelScopes()
    }

    private fun setupView()
    {
        /**
         * Set button TryAgain visibility based on function in VM
         */
        binding.butTryAgain.isVisible = viewModel.tryAgain != null
        viewModel.tryAgain?.let { tryAgain ->
            binding.butTryAgain.setOnClickListener {
                tryAgain()
            }
        }

        binding.txtEmptyState.text = getString(stateData.emptyStateText)
        binding.imgIconEmptyState.setImageResource(stateData.emptyStateIcon)

        binding.rvPosts.updatePadding(bottom = resources.getDimensionPixelOffset(stateData.bottomRecyclerPadding))
    }

    private fun setupRecycler()
    {
        postAdapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

        binding.rvPosts.adapter = postAdapter

        lifecycleScope.launchWhenStarted {
            viewModel.postToDisplay.collectLatest {
                when (it)
                {
                    GetStatus.Loading ->
                    {
                        setVisibility(State.LOADING)
                    }
                    is GetStatus.Failed ->
                    {
                        setVisibility(State.ERROR)
                        binding.txtErrorState.text = it.message.getFormattedMessage(binding.context)
                    }
                    is GetStatus.Success ->
                    {
                        if (it.data.isEmpty())
                        {
                            setVisibility(State.EMPTY)
                        }
                        else
                        {
                            setVisibility(State.SUCCESS)
                        }
                        postAdapter?.submitList(it.data.sortedByDescending { post ->
                            post.second.time
                        })
                    }
                }
            }
        }
    }

    private fun setVisibility(state: State)
    {
        with(binding)
        {
            proBarLoadingPosts.isVisible = state == State.LOADING
            linLayEmptyState.isVisible = state == State.EMPTY
            linLayErrorState.isVisible = state == State.ERROR
            rvPosts.isVisible = state == State.SUCCESS
        }
    }

    private enum class State
    {
        LOADING,
        EMPTY,
        ERROR,
        SUCCESS
    }
}

data class StateData(
    @StringRes val emptyStateText: Int,
    @DrawableRes val emptyStateIcon: Int,
    @DimenRes val bottomRecyclerPadding: Int = R.dimen.bottom_place_holder_home
)
