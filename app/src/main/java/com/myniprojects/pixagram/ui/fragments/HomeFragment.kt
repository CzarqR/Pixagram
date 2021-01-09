package com.myniprojects.pixagram.ui.fragments

import androidx.fragment.app.Fragment
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentHomeBinding
import com.myniprojects.pixagram.utils.viewBinding

class HomeFragment : Fragment(R.layout.fragment_home)
{
    private val binding by viewBinding(FragmentHomeBinding::bind)
}