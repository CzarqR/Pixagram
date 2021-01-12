package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.ImageAdapter
import com.myniprojects.pixagram.databinding.FragmentAddBinding
import com.myniprojects.pixagram.utils.Constants
import com.myniprojects.pixagram.utils.viewBinding
import com.myniprojects.pixagram.vm.AddViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class AddFragment : Fragment(R.layout.fragment_add)
{
    @Inject
    lateinit var imageAdapter: ImageAdapter

    private val binding by viewBinding(FragmentAddBinding::bind)
    private val viewModel: AddViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()

        viewModel.loadAllImages()
        setupCollecting()
    }

    private fun setupRecycler()
    {
        with(binding.rvGallery)
        {
            layoutManager = GridLayoutManager(requireContext(), Constants.GALLERY_COLUMNS)
            adapter = imageAdapter
        }
    }


    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.allImagesFromGallery.collectLatest {
                if (it.isNotEmpty())
                {
                    imageAdapter.submitList(it)
                }
            }
        }
    }
}