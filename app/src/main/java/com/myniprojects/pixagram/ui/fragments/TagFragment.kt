package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentTagBinding
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.ext.setActionBarTitle
import com.myniprojects.pixagram.vm.TagViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class TagFragment : Fragment(R.layout.fragment_tag)
{

    @Inject
    lateinit var glide: RequestManager

    private val binding by viewBinding(FragmentTagBinding::bind)
    private val viewModel: TagViewModel by activityViewModels()

    private val args: TagFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initTag(args.tag)
        setupCollecting()
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.tag.collectLatest {
                with(binding)
                {
                    txtPosts.text = resources.getQuantityString(
                        R.plurals.tag_counter_post,
                        it.count.toInt(),
                        it.count
                    )
                }

                setActionBarTitle(getString(R.string.tag_title_format, it.title))
            }
        }
    }
}