package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentUserBinding
import com.myniprojects.pixagram.utils.setActionBarTitle
import com.myniprojects.pixagram.utils.viewBinding
import com.myniprojects.pixagram.vm.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class UserFragment : Fragment(R.layout.fragment_user)
{
    @Inject
    lateinit var glide: RequestManager

    private val binding by viewBinding(FragmentUserBinding::bind)
    private val viewModel: UserViewModel by activityViewModels()

    private val args: UserFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initUser(args.user)
        setupCollecting()
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                with(binding)
                {
                    txtDesc.text = it.bio
                    txtFullName.text = it.fullName

                    glide
                        .load(it.imageUrl)
                        .into(imgAvatar)
                }
                setActionBarTitle(it.username)
            }
        }
    }
}