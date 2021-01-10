package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentProfileBinding
import com.myniprojects.pixagram.utils.viewBinding
import com.myniprojects.pixagram.vm.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest

class ProfileFragment : Fragment(R.layout.fragment_profile)
{
    private val viewModel: ProfileViewModel by activityViewModels()
    private val binding by viewBinding(FragmentProfileBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        setupCollecting()
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                if (it == null)
                {
                    findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
                }
            }
        }
    }
}