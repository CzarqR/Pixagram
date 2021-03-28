package com.myniprojects.pixagram.ui.fragments.utils

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.adapters.postadapter.PostClickListener
import javax.inject.Inject

/**
 * [FragmentPostRecycler] is a Fragment that displays posts in RecyclerView
 * and have implemented methods from [PostClickListener]
 */
abstract class FragmentPostRecycler(
    @LayoutRes layout: Int
) : FragmentPost(layout)
{
    @Inject
    lateinit var postAdapter: PostAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        postAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
        postAdapter.postClickListener = this
    }

    /**
     * When View is destroyed adapter should cancel scope in every ViewHolder
     */
    override fun onDestroyView()
    {
        super.onDestroyView()
        postAdapter.cancelScopes()
    }
}