package com.myniprojects.pixagram.adapters.imageadapter

import android.net.Uri
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.RequestManager
import javax.inject.Inject

class ImageAdapter @Inject constructor(
    private val glide: RequestManager
) : ListAdapter<Image, ImageViewHolder>(ImageDiffCallback)
{
    var clickListener: ((Uri) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder =
            ImageViewHolder.create(parent)

    override fun onBindViewHolder(holderImage: ImageViewHolder, position: Int) =
            holderImage.bind(
                image = getItem(position)!!,
                clickListener = clickListener,
                glide = glide
            )
}