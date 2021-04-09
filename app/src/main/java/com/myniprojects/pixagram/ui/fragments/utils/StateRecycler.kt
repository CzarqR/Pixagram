package com.myniprojects.pixagram.ui.fragments.utils

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.databinding.StateRecyclerBinding
import com.myniprojects.pixagram.utils.ext.StateData
import com.myniprojects.pixagram.utils.ext.setState
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.vm.utils.ViewModelStateRecycler
import kotlinx.coroutines.flow.collectLatest


class StateRecycler : Fragment(R.layout.state_recycler)
{
    private lateinit var viewModel: ViewModelStateRecycler
    private var postAdapter: PostAdapter? = null
    private lateinit var stateData: StateData

    val binding by viewBinding(StateRecyclerBinding::bind)

    fun scrollToTop()
    {
        binding.rvPosts.scrollToPosition(0)
    }

    fun animateShowHide()
    {
        val alphaAnimation = AlphaAnimation(1.0f, 0.0f).apply {
            duration = 400
            repeatCount = 1
            repeatMode = Animation.REVERSE
            interpolator = AccelerateInterpolator()
        }

        binding.rvPosts.startAnimation(alphaAnimation)
    }

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
                binding.setState(it, postAdapter)
            }
        }
    }
}