package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.imageadapter.ImageAdapter
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.databinding.FragmentHomeBinding
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.utils.viewBinding
import com.myniprojects.pixagram.vm.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home)
{
    @Inject
    lateinit var postAdapter: PostAdapter

    private val viewModel: HomeViewModel by activityViewModels()
    private val binding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        setupRecycler()
    }

    private fun setupRecycler()
    {
        binding.rvPosts.adapter = postAdapter

        // simple test, delete letter
        postAdapter.submitList(
            listOf(
                Post(
                    desc = "Some description, come home from long walk xD #life #life2 @czarek",
                    imageUrl = "https://firebasestorage.googleapis.com/v0/b/pixagram-5b72c.appspot.com/o/Posts%2Fsc8VCrf21vPiUCoiMTlTjt5zNsY2_1611671048729.jpg?alt=media&token=700c8276-20f3-4a5e-904d-9279a5f24676",
                    owner = "sD44TX3thAbSoiEPHWzrJ8k48n03",
                    postId = "-MRz3ncYidGER2cSK_tX"
                ),
                Post(
                    desc = "#life #life2 @czarek",
                    imageUrl = "https://firebasestorage.googleapis.com/v0/b/pixagram-5b72c.appspot.com/o/Posts%2Fsc8VCrf21vPiUCoiMTlTjt5zNsY2_1611671048729.jpg?alt=media&token=700c8276-20f3-4a5e-904d-9279a5f24676",
                    owner = "sD44TX3thAbSoiEPHWzrJ8k48n03",
                    postId = "-MRz3ncYidGER2cSK_tX"
                ),
                Post(
                    desc = "#life #life2 @czarek",
                    imageUrl = "https://firebasestorage.googleapis.com/v0/b/pixagram-5b72c.appspot.com/o/Posts%2Fsc8VCrf21vPiUCoiMTlTjt5zNsY2_1611671048729.jpg?alt=media&token=700c8276-20f3-4a5e-904d-9279a5f24676",
                    owner = "sD44TX3thAbSoiEPHWzrJ8k48n03",
                    postId = "-MRz3ncYidGER2cSK_tX"
                )
            )
        )

    }
}