package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentLoginBinding
import com.myniprojects.pixagram.utils.showSnackbar
import com.myniprojects.pixagram.utils.viewBinding
import com.myniprojects.pixagram.vm.LoginViewModel
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber


class LoginFragment : Fragment(R.layout.fragment_login)
{
    private val viewModel: LoginViewModel by activityViewModels()
    private val binding by viewBinding(FragmentLoginBinding::bind)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        setupCollecting()
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.loginState.collectLatest {
                when (it)
                {
                    LoginViewModel.LoginState.LOGIN -> setLoginState()
                    LoginViewModel.LoginState.REGISTRATION -> setRegistrationState()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.message.collectLatest { event ->
                Timber.d("Event retrieved $event")
                event?.getContentIfNotHandled()?.let { stringId ->
                    binding.root.showSnackbar(stringId)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.loading.collectLatest {
                binding.proBarLoading.isVisible = it
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                Timber.d("Logged user ${it.peekContent()}")
                it.getContentIfNotHandled()?.let {
                    successfulLogin()
                }
            }
        }
    }

    private fun successfulLogin()
    {
        viewModel.clearSensitiveData()
        findNavController().navigate(
            R.id.action_loginFragment_to_homeFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .build()
        )
    }


    private fun setLoginState()
    {
        with(binding)
        {
            butLogRegister.text = getString(R.string.log_in)
            txtLayPasswdConf.isVisible = false
            txtLayUsername.isVisible = false
            butChangeState.text = getString(R.string.create_account)
            (butChangeState as MaterialButton).setIconResource(R.drawable.ic_outline_person_add_24)
        }
    }

    private fun setRegistrationState()
    {
        with(binding)
        {
            butLogRegister.text = getString(R.string.register)
            txtLayPasswdConf.isVisible = true
            txtLayUsername.isVisible = true
            butChangeState.text = getString(R.string.have_account)
            (butChangeState as MaterialButton).setIconResource(R.drawable.ic_outline_login_24)
        }
    }


}