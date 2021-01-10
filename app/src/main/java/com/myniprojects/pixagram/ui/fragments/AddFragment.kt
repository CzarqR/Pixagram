package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentAddBinding
import com.myniprojects.pixagram.databinding.FragmentSearchBinding
import com.myniprojects.pixagram.utils.viewBinding

class AddFragment : Fragment(R.layout.fragment_add)
{
    private val binding by viewBinding(FragmentAddBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
    }
}