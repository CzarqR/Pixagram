package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentLoginBinding
import com.myniprojects.pixagram.ui.LoginActivity
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.ext.showSnackbar
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.FirebaseStatus
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
        binding.lifecycleOwner = this
        setupCollecting()
        setupClickListeners()
    }

    private fun setupClickListeners()
    {
        binding.butLogin.setOnClickListener {
            logRegister()
        }

        binding.butRegister.setOnClickListener {
            logRegister()
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
                    LoginViewModel.LoginState.LOGIN ->
                    {
                        binding.viewSwitcher.displayedChild = 0
                        setAnimation(true)
                    }
                    LoginViewModel.LoginState.REGISTRATION ->
                    {
                        binding.viewSwitcher.displayedChild = 1
                        setAnimation(false)
                    }
                }
            }
        }
    }

    private fun setAnimation(animToLeft: Boolean)
    {
        with(binding.viewSwitcher)
        {
            inAnimation = AnimationUtils.loadAnimation(
                requireContext(),
                if (animToLeft) R.anim.slide_in_right else R.anim.slide_in_left
            )
            outAnimation = AnimationUtils.loadAnimation(
                requireContext(),
                if (animToLeft) R.anim.slide_out_left else R.anim.slide_out_right
            )
        }
    }

    private fun logRegister()
    {
        lifecycleScope.launch {
            viewModel.logOrRegister().collectLatest {
                Timber.d("Collected status in LoginFragment: $it")
                when (it)
                {
                    FirebaseStatus.Sleep -> Unit //do nothing
                    FirebaseStatus.Loading ->
                    {
                        binding.proBarLoading.isVisible = true
                    }
                    is FirebaseStatus.Success ->
                    {
                        binding.proBarLoading.isVisible = false
                        binding.root.showSnackbar(it.message.getFormattedMessage(requireContext()))
                    }
                    is FirebaseStatus.Failed ->
                    {
                        binding.proBarLoading.isVisible = false
                        binding.root.showSnackbar(it.message.getFormattedMessage(requireContext()))
                    }
                }.exhaustive
            }
        }
    }
}