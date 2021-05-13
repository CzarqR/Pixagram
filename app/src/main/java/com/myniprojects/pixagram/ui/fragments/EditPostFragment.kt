package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.databinding.FragmentEditPostBinding
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.vm.EditPostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class EditPostFragment : Fragment(R.layout.fragment_edit_post)
{
    @Inject
    lateinit var glide: RequestManager

    private val binding by viewBinding(FragmentEditPostBinding::bind)
    val viewModel: EditPostViewModel by viewModels()

    private val args: EditPostFragmentArgs by navArgs()
    private lateinit var post: PostWithId

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        post = args.postId to args.post
        setupCollecting()
        setupView()
        viewModel.initPost(post)
    }

    private fun setupView()
    {
        binding.edTxtDesc.doAfterTextChanged {
            viewModel.updateDesc(it.toString())
        }

        binding.fabSave.setOnClickListener {
            viewModel.save()
        }
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.basePost.collectLatest {
                glide
                    .load(it.imageUrl)
                    .into(binding.img)

                binding.edTxtDesc.setText(it.desc)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.isAnythingChanged.collectLatest {
                binding.fabSave.isVisible = it
            }
        }
    }
}