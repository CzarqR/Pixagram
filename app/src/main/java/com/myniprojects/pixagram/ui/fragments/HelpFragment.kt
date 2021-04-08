package com.myniprojects.pixagram.ui.fragments

import androidx.fragment.app.Fragment
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentHomeBinding
import com.myniprojects.pixagram.utils.ext.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HelpFragment : Fragment(R.layout.fragment_help)
{
    private val binding by viewBinding(FragmentHomeBinding::bind)
}