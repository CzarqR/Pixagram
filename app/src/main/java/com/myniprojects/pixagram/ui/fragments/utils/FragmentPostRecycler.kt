package com.myniprojects.pixagram.ui.fragments.utils

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.adapters.postadapter.PostClickListener
import com.myniprojects.pixagram.vm.utils.ViewModelPostRecycler
import javax.inject.Inject

/**
 * [FragmentPostRecycler] is a Fragment that displays posts in RecyclerView
 * and have implemented methods from [PostClickListener]
 */
abstract class FragmentPostRecycler(
    @LayoutRes layout: Int,
    private val stateData: StateData
) : FragmentPost(layout)
{
    abstract override val viewModel: ViewModelPostRecycler

    /**
     * [PostAdapter] is injected in parent [FragmentPostRecycler]
     * would be better if it was injected directly in [FragmentRecycler]
     * but this causes error
     */
    @Inject
    lateinit var postAdapter: PostAdapter

    /**
     * Every Fragment that extends [FragmentPostRecycler]
     * must have FragmentContainerView with id `R.id.fragmentRecycler`
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val fragmentRecycler = FragmentRecycler(viewModel, this, postAdapter, stateData)
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentRecycler, fragmentRecycler).commit()
    }

}