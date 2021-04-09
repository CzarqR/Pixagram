package com.myniprojects.pixagram.adapters.postcategoryadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.databinding.StateRecyclerBinding

class RecyclerStateViewHolder private constructor(
    private val binding: StateRecyclerBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): RecyclerStateViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = StateRecyclerBinding.inflate(layoutInflater, parent, false)
            return RecyclerStateViewHolder(
                binding
            )
        }
    }

    fun bind(
        stateRecyclerData: StateRecyclerData
    )
    {

    }

}
