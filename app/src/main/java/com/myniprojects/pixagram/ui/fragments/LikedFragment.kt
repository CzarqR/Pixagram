package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentLoginBinding
import com.myniprojects.pixagram.databinding.FragmentSearchBinding
import com.myniprojects.pixagram.utils.viewBinding

class LikedFragment : Fragment(R.layout.fragment_liked)
{
    private val binding by viewBinding(FragmentLoginBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
    }
}