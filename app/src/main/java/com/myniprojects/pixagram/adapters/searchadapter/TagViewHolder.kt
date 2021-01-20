package com.myniprojects.pixagram.adapters.searchadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.TagItemBinding
import com.myniprojects.pixagram.model.Tag

class TagViewHolder private constructor(
    private val binding: TagItemBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): TagViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = TagItemBinding.inflate(layoutInflater, parent, false)
            return TagViewHolder(
                binding
            )
        }
    }


    fun bind(
        tag: Tag,
        clickListener: ((Tag) -> Unit)?
    )
    {
        with(binding)
        {
            txtTitle.text = binding.root.context.getString(R.string.tag_title_format, tag.title)
            txtCount.text = binding.root.context.getString(R.string.tag_counter_format, tag.count)

            clickListener?.let { click ->
                root.setOnClickListener {
                    click(tag)
                }
            }
        }
    }
}