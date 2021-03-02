package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostAdapter
import com.myniprojects.pixagram.databinding.FragmentTagBinding
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.ext.setActionBarTitle
import com.myniprojects.pixagram.utils.ext.viewBinding
import com.myniprojects.pixagram.utils.status.DataStatus
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.vm.TagViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TagFragment : Fragment(R.layout.fragment_tag)
{

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var postAdapter: PostAdapter

    private val binding by viewBinding(FragmentTagBinding::bind)
    private val viewModel: TagViewModel by activityViewModels()

    private val args: TagFragmentArgs by navArgs()

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initTag(args.tag)
        setActionBarTitle(getString(R.string.tag_title_format, args.tag.title))
        setupCollecting()
        setupRecycler()
    }

    private fun setupCollecting()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.tag.collectLatest {

                when (it)
                {
                    is GetStatus.Failed ->
                    {
                    }
                    GetStatus.Loading ->
                    {
                        with(binding)
                        {
                            txtPosts.text = getString(R.string.loading_posts_counter)
                        }
                    }
                    is GetStatus.Success ->
                    {
                        with(binding)
                        {
                            txtPosts.text = resources.getQuantityString(
                                R.plurals.tag_counter_post,
                                it.data.count.toInt(),
                                it.data.count
                            )
                        }
                        setActionBarTitle(getString(R.string.tag_title_format, it.data.title))

                    }
                }.exhaustive


            }
        }
    }

    /**
     * Todo display properly: loading / error / empty list
     */
    private fun setupRecycler()
    {
//        postAdapter.commentListener = { postId ->
//            val action = UserFragmentDirections.actionUserFragmentToCommentFragment(
//                postId = postId
//            )
//            findNavController().navigate(action)
//        }

        binding.rvPosts.adapter = postAdapter

        /**
         * Collect selected user posts
         */
        lifecycleScope.launchWhenStarted {
            viewModel.posts.collectLatest { postsStatus ->
                Timber.d(postsStatus.toString())

                when (postsStatus)
                {
                    DataStatus.Loading ->
                    {
                    }
                    is DataStatus.Success ->
                    {
                        val l = postsStatus.data.toList().sortedByDescending {
                            it.second.time
                        }
                        postAdapter.submitList(l)

                        if (l.isNotEmpty())
                        {
                            glide
                                .load(l[0].second.imageUrl)
                                .into(binding.imgTag)
                        }
                    }
                    is DataStatus.Failed ->
                    {
                    }
                }
            }
        }
    }
}