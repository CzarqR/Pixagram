package com.myniprojects.pixagram.ui.fragments

import androidx.fragment.app.Fragment
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentMessagesBinding
import com.myniprojects.pixagram.utils.ext.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessagesFragment : Fragment(R.layout.fragment_messages)
{
    private val binding by viewBinding(FragmentMessagesBinding::bind)
}