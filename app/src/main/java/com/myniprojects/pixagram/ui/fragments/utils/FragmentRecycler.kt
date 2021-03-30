package com.myniprojects.pixagram.ui.fragments.utils

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.adapters.postadapter.PostClickListener
import com.myniprojects.pixagram.databinding.PostRecyclerBinding
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.DataStatus
import com.myniprojects.pixagram.vm.ViewModelPostRecycler
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

class FragmentRecycler(
    private val viewModel: ViewModelPostRecycler,
    private val postClickListener: PostClickListener,
    private val postAdapter: PostAdapter
) : Fragment(R.layout.post_recycler)
{
    val binding by viewBinding(PostRecyclerBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.proBarLoadingPosts.isVisible = false
        setupRecycler()
    }

    /**
     * When View is destroyed adapter should cancel scope in every ViewHolder
     */
    override fun onDestroyView()
    {
        super.onDestroyView()
        postAdapter.cancelScopes()
    }

    private fun setupRecycler()
    {
        postAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
        postAdapter.postClickListener = postClickListener

        binding.rvPosts.adapter = postAdapter

        lifecycleScope.launchWhenStarted {
            viewModel.postToDisplay.collectLatest {
                when (it)
                {
                    DataStatus.Loading ->
                    {
                        Timber.d("Loading - FragmentRecycler")
                        binding.proBarLoadingPosts.isVisible = true
                        binding.rvPosts.isVisible = false
                    }
                    is DataStatus.Failed ->
                    {
                        Timber.d("Failed - FragmentRecycler")
                    }
                    is DataStatus.Success ->
                    {
                        Timber.d("Success - FragmentRecycler ${it.data}")
                        binding.proBarLoadingPosts.isVisible = false
                        binding.rvPosts.isVisible = true
                        postAdapter.submitList(it.data.toList())
                    }
                }
            }
        }
    }
}
