package com.myniprojects.pixagram.ui.fragments

import androidx.fragment.app.Fragment
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentSettingsBinding
import com.myniprojects.pixagram.utils.ext.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings)
{
    private val binding by viewBinding(FragmentSettingsBinding::bind)
}