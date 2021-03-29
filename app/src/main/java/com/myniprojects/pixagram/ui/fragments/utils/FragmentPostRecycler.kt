package com.myniprojects.pixagram.ui.fragments.utils

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.R
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
     * Below are fields that every fragment must have
     * probably it's not possible to abstract ViewBinding
     * (https://stackoverflow.com/questions/66852816/viewbinding-abstract-class-or-interface)
     * so this is not type safe
     */
    lateinit var rvPosts: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(binding.root)
        {
            rvPosts = findViewById(R.id.rvPosts)
        }

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
        postAdapter.postClickListener = this

        rvPosts.adapter = postAdapter
    }
}