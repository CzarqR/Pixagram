package com.myniprojects.pixagram.ui.fragments

import androidx.fragment.app.Fragment
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.EditProfileFragmentBinding
import com.myniprojects.pixagram.utils.ext.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.edit_profile_fragment)
{
    private val binding by viewBinding(EditProfileFragmentBinding::bind)
}