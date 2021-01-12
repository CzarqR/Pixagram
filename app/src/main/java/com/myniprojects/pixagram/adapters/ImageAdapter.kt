package com.myniprojects.pixagram.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.databinding.ImageItemBinding
import javax.inject.Inject

class ImageAdapter @Inject constructor(
    private val glide: RequestManager
) : ListAdapter<Uri, ImageAdapter.ViewHolder>(ImageDiffCallback)
{
    var clickListener: ((Uri) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(
                uri = getItem(position)!!,
                clickListener = clickListener,
                glide = glide
            )


    class ViewHolder private constructor(private val binding: ImageItemBinding) :
            RecyclerView.ViewHolder(binding.root)
    {
        companion object
        {
            fun from(parent: ViewGroup): ViewHolder
            {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ImageItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(
                    binding
                )
            }
        }


        fun bind(
            uri: Uri,
            clickListener: ((Uri) -> Unit)?,
            glide: RequestManager
        )
        {
            with(binding)
            {
                glide
                    .load(uri)
                    .into(img)

                clickListener?.let { click ->
                    img.setOnClickListener {
                        click(uri)
                    }
                }
            }
        }
    }
}

object ImageDiffCallback : DiffUtil.ItemCallback<Uri>()
{
    override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean =
            oldItem == newItem

    override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean =
            oldItem == newItem

}