package com.myniprojects.pixagram.ui.fragments.utils

import androidx.annotation.LayoutRes
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

    /**
     * When View is destroyed adapter should cancel scope in every ViewHolder
     */
    override fun onDestroyView()
    {
        super.onDestroyView()
        postAdapter.cancelScopes()
    }
}