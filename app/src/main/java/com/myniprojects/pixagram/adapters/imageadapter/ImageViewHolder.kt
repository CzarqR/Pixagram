package com.myniprojects.pixagram.adapters.imageadapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.ImageItemBinding

class ImageViewHolder private constructor(
    private val binding: ImageItemBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): ImageViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ImageItemBinding.inflate(layoutInflater, parent, false)
            return ImageViewHolder(
                binding
            )
        }
    }


    fun bind(
        image: Image,
        clickListener: ((Uri) -> Unit)?,
        glide: RequestManager
    )
    {
        with(binding)
        {
            glide
                .load(image.uri)
                .into(img)

            clickListener?.let { click ->
                img.setOnClickListener {
                    click(image.uri)
                }
            }

            imgSelection.isVisible = image.isSelected
            img.alpha = if (image.isSelected) 0.4F else 1F


            img.setPadding(
                if (image.isSelected)
                {
                    img.context.resources.getDimensionPixelSize(R.dimen.medium_margin)
                }
                else
                {
                    img.context.resources.getDimensionPixelSize(R.dimen.small_margin)
                }
            )
        }
    }
}