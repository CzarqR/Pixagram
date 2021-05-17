package com.myniprojects.pixagram.ui.fragments.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostClickListener
import com.myniprojects.pixagram.adapters.useradapter.UserAdapter
import com.myniprojects.pixagram.ui.MainActivity
import com.myniprojects.pixagram.utils.consts.Constants
import com.myniprojects.pixagram.utils.ext.getShareIntent
import com.myniprojects.pixagram.utils.ext.input
import com.myniprojects.pixagram.utils.ext.showSnackbarGravity
import com.myniprojects.pixagram.utils.ext.tryOpenUrl
import com.myniprojects.pixagram.utils.status.GetStatus
import com.myniprojects.pixagram.vm.utils.ViewModelPost
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

/**
 * [AbstractFragmentPost] is a Fragment that displays posts
 * and have implemented methods from [PostClickListener]
 * Fragments should be scoped in [MainActivity]
 */
abstract class AbstractFragmentPost(
    @LayoutRes layout: Int
) : Fragment(layout), PostClickListener
{
    @Inject
    lateinit var userAdapter: UserAdapter

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

    override fun shareClick(postId: String)
    {
        startActivity(getShareIntent(Constants.getShareLinkToPost(postId)))
    }

    private var searchUsersJob: Job? = null

    override fun likeCounterClick(postId: String)
    {
        val d = LayoutInflater.from(requireContext())
            .inflate(R.layout.users_dialog, null, false)

        val rvUsers = d.findViewById<RecyclerView>(R.id.rvUsers)
        val proBarLoading = d.findViewById<ProgressBar>(R.id.proBarLoading)
        val txtInfo = d.findViewById<MaterialTextView>(R.id.txtInfo)
        rvUsers.adapter = userAdapter

        materialAlertDialogBuilder.setView(d)
            .setTitle(getString(R.string.users_that_like_post))
            .setPositiveButton(R.string.close) { dialog, _ ->
                dialog.cancel()
            }
            .setOnCancelListener {
                searchUsersJob?.cancel()
            }
            .show()

        searchUsersJob = lifecycleScope.launchWhenStarted {
            viewModel.getUsersThatLikePost(postId).collectLatest {
                when (it)
                {
                    GetStatus.Sleep -> Unit
                    GetStatus.Loading ->
                    {
                        rvUsers.isVisible = false
                        proBarLoading.isVisible = true
                        txtInfo.isVisible = false
                    }
                    is GetStatus.Success ->
                    {
                        proBarLoading.isVisible = false

                        if (it.data.isNotEmpty())
                        {
                            rvUsers.isVisible = true
                            txtInfo.isVisible = false
                        }
                        else
                        {
                            rvUsers.isVisible = false
                            txtInfo.isVisible = true
                            txtInfo.setText(R.string.empty_users_liking_post)
                        }
                    }
                    is GetStatus.Failed ->
                    {
                        rvUsers.isVisible = false
                        proBarLoading.isVisible = false
                        txtInfo.isVisible = true
                        txtInfo.setText(R.string.something_went_wrong_loading_users_that_liked_post)

                    }
                }
            }
        }
    }


    override fun linkClick(link: String)
    {
        Timber.d("Link clicked $link")
        requireContext().tryOpenUrl(link) {
            showSnackbar(R.string.could_not_open_browser)
        }
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


    // endregion

    override fun onDestroy()
    {
        super.onDestroy()
        userAdapter.cancelScopes()
    }

}