package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.commentadapter.CommentAdapter
import com.myniprojects.pixagram.databinding.FragmentCommentBinding
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.ext.input
import com.myniprojects.pixagram.utils.ext.showSnackbarGravity
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.DataStatus
import com.myniprojects.pixagram.utils.status.FirebaseStatus
import com.myniprojects.pixagram.vm.CommentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CommentFragment : Fragment(R.layout.fragment_comment)
{
    @Inject
    lateinit var adapter: CommentAdapter

    private val viewModel: CommentViewModel by viewModels()
    private val binding by viewBinding(FragmentCommentBinding::bind)

    private val args: CommentFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        setClickListeners()
    }

    private fun setupRecycler()
    {
        binding.rvComments.adapter = adapter

        lifecycleScope.launchWhenStarted {
            viewModel.getComments(args.postId).collectLatest {
                Timber.d("Comment status: $it")
                when (it)
                {
                    DataStatus.Loading ->
                    {
                        binding.progressBarComments.isVisible = true
                        showNoCommentsInfo(false)
                    }
                    is DataStatus.Success ->
                    {
                        /**
                         * List is sorted every time. Try to find better solution
                         */
                        adapter.submitList(it.data.toList().sortedBy { comment ->
                            comment.second.time
                        })
                        binding.progressBarComments.isVisible = false

                        showNoCommentsInfo(it.data.count() == 0)


                    }
                    is DataStatus.Failed ->
                    {
                        binding.progressBarComments.isVisible = false
                        showNoCommentsInfo(false)
                        binding.root.showSnackbarGravity(
                            message = getString(R.string.something_went_wrong)
                        )
                    }
                }.exhaustive
            }
        }
    }

    private fun showNoCommentsInfo(isVisible: Boolean)
    {
        binding.imgCommentIcon.isVisible = isVisible
        binding.txtNoComments.isVisible = isVisible
        binding.txtWrite.isVisible = isVisible
    }

    private fun setClickListeners()
    {
        with(binding)
        {
            butPost.setOnClickListener {

                lifecycleScope.launch {
                    viewModel.addComment(args.postId, edTxtComment.input).collectLatest {

                        when (it)
                        {
                            FirebaseStatus.Sleep -> Unit
                            FirebaseStatus.Loading ->
                            {
                                binding.progressBarPost.isVisible = true
                                butPost.isEnabled = false
                            }
                            is FirebaseStatus.Failed ->
                            {
                                binding.progressBarPost.isVisible = false
                                butPost.isEnabled = true
                                binding.root.showSnackbarGravity(
                                    message = it.message.getFormattedMessage(requireContext())
                                )
                            }
                            is FirebaseStatus.Success ->
                            {
                                binding.progressBarPost.isVisible = false
                                butPost.isEnabled = true
                                binding.edTxtComment.setText("")
                            }
                        }.exhaustive

                        Timber.d("Collected $it")
                    }
                }


            }
        }
    }

}