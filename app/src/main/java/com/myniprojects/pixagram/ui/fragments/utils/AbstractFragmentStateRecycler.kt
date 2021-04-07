package com.myniprojects.pixagram.ui.fragments.utils

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.adapters.postadapter.PostClickListener
import com.myniprojects.pixagram.vm.utils.ViewModelStateRecycler
import javax.inject.Inject

/**
 * [AbstractFragmentStateRecycler] is a Fragment that displays posts in RecyclerView
 * and have implemented methods from [PostClickListener]
 */
abstract class AbstractFragmentStateRecycler(
    @LayoutRes layout: Int,
    private val stateData: StateData
) : AbstractFragmentPost(layout)
{
    abstract override val viewModel: ViewModelStateRecycler

    /**
     * [PostAdapter] is injected in parent [AbstractFragmentStateRecycler]
     * would be better if it was injected directly in [StateRecycler]
     * but this causes error
     */
    @Inject
    lateinit var postAdapter: PostAdapter

    /**
     * Every Fragment that extends [AbstractFragmentStateRecycler]
     * must have FragmentContainerView with id `R.id.fragmentRecycler`
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        postAdapter.postClickListener = this
        val fragmentRecycler = StateRecycler()
        fragmentRecycler.initView(viewModel, postAdapter, stateData)

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentRecycler, fragmentRecycler).commit()
    }
}