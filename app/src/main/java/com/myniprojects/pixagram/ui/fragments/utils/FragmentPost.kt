package com.myniprojects.pixagram.ui.fragments.utils

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostClickListener
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.ui.MainActivity
import com.myniprojects.pixagram.utils.ext.showToastNotImpl
import com.myniprojects.pixagram.vm.ViewModelPost
import timber.log.Timber

/**
 * [FragmentPost] is a Fragment that displays posts
 * and have implemented methods from [PostClickListener]
 */
abstract class FragmentPost(
    @LayoutRes layout: Int
) : Fragment(layout), PostClickListener
{
    protected abstract val viewModel: ViewModelPost

    abstract fun showSnackbar(@StringRes message: Int)

    // region post events

    override fun likeClick(postId: String, status: Boolean)
    {
        viewModel.setLikeStatus(postId, status)
    }

    override fun commentClick(postId: String)
    {
        showToastNotImpl()
    }

    override fun shareClick(postId: String)
    {
        showToastNotImpl()
    }

    override fun likeCounterClick(postId: String)
    {
        showToastNotImpl()
    }

    override fun profileClick(postOwner: String)
    {
        showToastNotImpl()
    }

    override fun imageClick(postWithId: PostWithId)
    {
        showToastNotImpl()
    }

    override fun tagClick(tag: String)
    {
        showToastNotImpl()
    }

    override fun linkClick(link: String)
    {
        Timber.d("Link clicked $link")
        (activity as MainActivity).tryOpenUrl(link) {
            showSnackbar(R.string.could_not_open_browser)
        }
    }

    override fun mentionClick(mention: String)
    {
        showToastNotImpl()
    }

    override fun menuReportClick(postId: String)
    {
        showToastNotImpl()
    }

    // endregion

}