package com.myniprojects.pixagram.ui.fragments.utils

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostClickListener
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.ui.MainActivity
import com.myniprojects.pixagram.utils.consts.Constants
import com.myniprojects.pixagram.utils.ext.getShareIntent
import com.myniprojects.pixagram.utils.ext.showSnackbarGravity
import com.myniprojects.pixagram.utils.ext.showToastNotImpl
import com.myniprojects.pixagram.utils.ext.tryOpenUrl
import com.myniprojects.pixagram.vm.utils.ViewModelPost
import timber.log.Timber

/**
 * [AbstractFragmentPost] is a Fragment that displays posts
 * and have implemented methods from [PostClickListener]
 * Fragments should be scoped in [MainActivity]
 */
abstract class AbstractFragmentPost(
    @LayoutRes layout: Int
) : Fragment(layout), PostClickListener
{
    protected abstract val viewModel: ViewModelPost

    protected abstract val binding: ViewBinding

    /**
     * When layout root is not a CoordinatorLayout
     * this field has to be overridden
     */
    @Suppress("SameParameterValue")
    protected open fun showSnackbar(@StringRes message: Int)
    {
        (binding.root as? CoordinatorLayout)?.showSnackbarGravity(getString(message))
    }

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
        startActivity(getShareIntent(Constants.getShareLinkToPost(postId)))
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
        requireContext().tryOpenUrl(link) {
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

    override fun menuEditClick(postId: String)
    {
        showToastNotImpl()
    }

    // endregion

}