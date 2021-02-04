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
import com.myniprojects.pixagram.ui.LoginActivity
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.showSnackbar
import com.myniprojects.pixagram.utils.status.LoginRegisterStatus
import com.myniprojects.pixagram.vm.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login)
{
    private val viewModel: LoginViewModel by activityViewModels()
    private val binding by viewBinding(FragmentLoginBinding::bind)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        setupCollecting()
        setupClickListeners()
    }

    private fun setupClickListeners()
    {
        binding.butLogRegister.setOnClickListener {
            lifecycleScope.launch {
                viewModel.logOrRegister().collectLatest {
                    Timber.d("Collected status in LoginFragment: $it")
                    when (it)
                    {
                        LoginRegisterStatus.Loading ->
                        {
                            binding.proBarLoading.isVisible = true
                        }
                        is LoginRegisterStatus.Success ->
                        {
                            binding.proBarLoading.isVisible = false
                            binding.root.showSnackbar(it.message.getFormattedMessage(requireContext()))
                        }
                        is LoginRegisterStatus.Failed ->
                        {
                            binding.proBarLoading.isVisible = false
                            binding.root.showSnackbar(it.message.getFormattedMessage(requireContext()))
                        }
                    }.exhaustive
                }
            }
        }
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                /**
                check if user is logged.
                if so change activity and go to [com.myniprojects.pixagram.ui.MainActivity]
                 */
                it?.let {
                    (activity as LoginActivity).navigateToMain()
                }
            }
        }

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

    private fun setLoginState()
    {
        with(binding)
        {
            butLogRegister.text = getString(R.string.log_in)
            txtLayPasswdConf.isVisible = false
            txtLayUsername.isVisible = false
            txtLayFullname.isVisible = false
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
            txtLayFullname.isVisible = true
            butChangeState.text = getString(R.string.have_account)
            (butChangeState as MaterialButton).setIconResource(R.drawable.ic_outline_login_24)
        }
    }
}