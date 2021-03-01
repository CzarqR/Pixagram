package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import coil.request.ImageRequest
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentDetailPostBinding
import com.myniprojects.pixagram.utils.ext.getDateTimeFormat
import com.myniprojects.pixagram.utils.ext.setActionBarTitle
import com.myniprojects.pixagram.utils.ext.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DetailPostFragment : Fragment(R.layout.fragment_detail_post)
{
    @Inject
    lateinit var imageLoader: ImageLoader

    private val binding by viewBinding(FragmentDetailPostBinding::bind)
    private val args: DetailPostFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        with(binding)
        {

            val request = ImageRequest.Builder(requireContext())
                .data(args.post.imageUrl)
                .target { drawable ->
                    binding.imgPost.setImageDrawable(drawable)
                }
                .build()
            imageLoader.enqueue(request)

            txtDesc.text = args.post.desc
            txtTime.text = args.post.time.getDateTimeFormat()
            setActionBarTitle(args.post.desc)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_toolbar_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.miLike -> like()
            R.id.miComment -> comment()
            R.id.miShare -> share()

        }
        return super.onOptionsItemSelected(item)
    }

    private fun share()
    {
        Timber.d("Share")
    }

    private fun comment()
    {
        Timber.d("Comment")
    }

    private fun like()
    {
        Timber.d("Like")
    }
}