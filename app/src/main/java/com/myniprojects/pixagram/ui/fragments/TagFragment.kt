package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentTagBinding
import com.myniprojects.pixagram.databinding.FragmentUserBinding
import com.myniprojects.pixagram.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TagFragment : Fragment(R.layout.fragment_tag)
{
    private val binding by viewBinding(FragmentTagBinding::bind)

    private val args: TagFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("Tag: ${args.tag}")

    }
}