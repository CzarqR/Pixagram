package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentCommentBinding
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.ext.input
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.FirebaseStatus
import com.myniprojects.pixagram.vm.CommentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CommentFragment : Fragment(R.layout.fragment_comment)
{
    private val viewModel: CommentViewModel by activityViewModels()
    private val binding by viewBinding(FragmentCommentBinding::bind)

    private val args: CommentFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
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