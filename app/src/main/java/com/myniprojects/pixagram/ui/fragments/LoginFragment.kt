package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentLoginBinding
import com.myniprojects.pixagram.vm.LoginViewModel
import kotlinx.coroutines.flow.collectLatest

class LoginFragment : Fragment(R.layout.fragment_login)
{
    private val viewModel: LoginViewModel by activityViewModels()
    private lateinit var binding: FragmentLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        setupCollecting()
        setupClickListeners()
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
    }

    private fun setupClickListeners()
    {
        with(binding)
        {
            butChangeState.setOnClickListener {
                viewModel.changeState()
            }
        }
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