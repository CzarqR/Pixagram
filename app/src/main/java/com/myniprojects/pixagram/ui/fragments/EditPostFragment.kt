package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.hendraanggrian.appcompat.widget.SocialEditText
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.databinding.FragmentEditPostBinding
import com.myniprojects.pixagram.utils.ext.showSnackbarGravity
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.EventMessageStatus
import com.myniprojects.pixagram.vm.EditPostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class EditPostFragment : Fragment(R.layout.fragment_edit_post)
{
    private lateinit var helper: SocialEditText

    @Inject
    lateinit var glide: RequestManager

    private val binding by viewBinding(FragmentEditPostBinding::bind)
    val viewModel: EditPostViewModel by viewModels()

    private val args: EditPostFragmentArgs by navArgs()
    private lateinit var post: PostWithId

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        helper = SocialEditText(requireContext())
        post = args.postId to args.post
        setupCollecting()
        setupView()
        viewModel.initPost(post)
    }

    private fun setupView()
    {
        binding.edTxtDesc.doAfterTextChanged {
            viewModel.updateDesc(it.toString())
        }

        binding.fabSave.setOnClickListener {

            val base = viewModel.basePost.value
            val new = viewModel.updatedPost.value


            helper.setText(base.desc)
            val oldHashtags = helper.hashtags
            val oldMentions = helper.mentions

            helper.setText(new.desc)
            val newHashtags = helper.hashtags
            val newMentions = helper.mentions
            lifecycleScope.launchWhenStarted {
                viewModel.save(newHashtags, newMentions, oldHashtags, oldMentions)
                    .collectLatest { status ->
                        Timber.d("Status $status")

                        when (status)
                        {
                            EventMessageStatus.Sleep ->
                            {
                                binding.progressBarEdit.isVisible = false
                            }
                            EventMessageStatus.Loading ->
                            {
                                binding.progressBarEdit.isVisible = true
                            }
                            is EventMessageStatus.Success ->
                            {
                                binding.progressBarEdit.isVisible = false
                                status.eventMessage.getContentIfNotHandled()?.let { msg ->
                                    Timber.d("Show msg $msg")
                                    binding.cdRoot.showSnackbarGravity(
                                        msg.getFormattedMessage(
                                            requireContext()
                                        )
                                    )
                                }
                            }
                            is EventMessageStatus.Failed ->
                            {
                                binding.progressBarEdit.isVisible = false
                                status.eventMessage.getContentIfNotHandled()?.let { msg ->
                                    binding.cdRoot.showSnackbarGravity(
                                        msg.getFormattedMessage(
                                            requireContext()
                                        )
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.basePost.collectLatest {
                glide
                    .load(it.imageUrl)
                    .into(binding.img)

                binding.edTxtDesc.setText(it.desc)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.isAnythingChanged.collectLatest {
                binding.fabSave.isVisible = it
            }
        }
    }
}