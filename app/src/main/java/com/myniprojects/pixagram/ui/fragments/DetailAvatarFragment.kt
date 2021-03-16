package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import coil.request.ImageRequest
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentDetailAvatarBinding
import com.myniprojects.pixagram.utils.ext.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class DetailAvatarFragment : Fragment(R.layout.fragment_detail_avatar)
{
    @Inject
    lateinit var imageLoader: ImageLoader

    private val binding by viewBinding(FragmentDetailAvatarBinding::bind)
    private val args: DetailAvatarFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val request = ImageRequest.Builder(requireContext())
            .data(args.avatarUrl)
            .target { drawable ->
                binding.imgPost.setImageDrawable(drawable)
            }
            .build()
        imageLoader.enqueue(request)

        binding.butBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}