package com.myniprojects.pixagram.ui.fragments.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostClickListener
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.ui.MainActivity
import com.myniprojects.pixagram.utils.consts.Constants
import com.myniprojects.pixagram.utils.ext.*
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

    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
    }

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
        val d = LayoutInflater.from(requireContext())
            .inflate(R.layout.report_dialog, null, false)

        val reportTextField = d.findViewById<TextInputEditText>(R.id.edTxtReportText)

        materialAlertDialogBuilder.setView(d)
            .setTitle(getString(R.string.report_post))
            .setMessage(getString(R.string.report_tip))
            .setPositiveButton(getString(R.string.report)) { dialog, _ ->
                val reportText = reportTextField.input
                Timber.d("Report send $reportText")
                dialog.dismiss()
                viewModel.reportPost(postId, reportText)
                showSnackbar(R.string.thanks_for_reporting)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun menuEditClick(postId: String)
    {
        showToastNotImpl()
    }

    // endregion

}