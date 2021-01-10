package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentHomeBinding
import com.myniprojects.pixagram.utils.viewBinding
import com.myniprojects.pixagram.vm.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home)
{
    private val viewModel: HomeViewModel by activityViewModels()
    private val binding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        setupObservers()
    }

    private fun setupObservers()
    {
        lifecycleScope.launch {
            viewModel.user.collectLatest {
                if (it == null)
                {
                    signOut()
                }
            }
        }
    }

    private fun signOut()
    {
        findNavController().navigate(
            R.id.action_homeFragment_to_loginFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, true)
                .build()
        )
    }

}