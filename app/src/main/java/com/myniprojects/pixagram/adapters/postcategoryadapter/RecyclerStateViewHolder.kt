package com.myniprojects.pixagram.adapters.postcategoryadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.pixagram.databinding.StateRecyclerBinding
import com.myniprojects.pixagram.utils.ext.setState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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


    private lateinit var stateRecyclerData: StateRecyclerData

    private val scope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null

    fun bind(
        stateRecyclerData: StateRecyclerData
    )
    {
        binding.rvPosts.adapter = stateRecyclerData.postAdapter

        job?.cancel()

        job = scope.launch {
            stateRecyclerData.postsToDisplay.collectLatest {
                binding.setState(it, stateRecyclerData.postAdapter)
            }
        }
    }

}
