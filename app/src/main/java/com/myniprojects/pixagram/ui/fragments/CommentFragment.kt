package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentCommentBinding
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.vm.CommentViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CommentFragment : Fragment(R.layout.fragment_comment)
{
    private val viewModel: CommentViewModel by activityViewModels()
    private val binding by viewBinding(FragmentCommentBinding::bind)

    private val args: CommentFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        Timber.d(args.postId)
    }

}