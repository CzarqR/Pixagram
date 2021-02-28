package com.myniprojects.pixagram.adapters.simplepostadapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.adapters.postadapter.PostDiffCallback
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import javax.inject.Inject

class SimplePostAdapter @Inject constructor(
    private val glide: RequestManager,
) : ListAdapter<PostWithId, SimplePostViewHolder>(PostDiffCallback)
{
    var postListener: (String) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimplePostViewHolder =
            SimplePostViewHolder.create(parent)

    override fun onBindViewHolder(holder: SimplePostViewHolder, position: Int) =
            holder.bind(
                post = getItem(position),
                glide = glide,
                postListener = postListener
            )
}