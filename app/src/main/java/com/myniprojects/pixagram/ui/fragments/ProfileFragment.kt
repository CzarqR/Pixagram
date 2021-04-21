package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.postadapter.PostWithId
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.ui.fragments.utils.AbstractUserFragment
import com.myniprojects.pixagram.utils.ext.showSnackbarGravity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ProfileFragment : AbstractUserFragment()
{
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
        Timber.d("Created")
        if (!viewModel.isInitialized.value)
        {
            viewModel.initWithLoggedUser()
        }
        else
        {
            viewModel.refreshUser()
        }

        setupView()
        setupBaseCollecting()
        initRecyclers(true)
        setupClickListener()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_toolbar_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            R.id.miSettings ->
            {
                findNavController().navigate(R.id.settingsFragment)
                true
            }
            R.id.miSignOut ->
            {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.sign_out))
                    .setMessage(resources.getString(R.string.log_out_confirmation))
                    .setNeutralButton(resources.getString(R.string.cancel)) { _, _ ->
                    }
                    .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                        viewModel.signOut()
                    }
                    .show()

                true
            }
            R.id.miEdit ->
            {
                findNavController().navigate(R.id.editProfileFragment)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setupView()
    {
        binding.buttonsArea.isVisible = false
    }

    private fun setupClickListener()
    {
        binding.imgAvatar.setOnClickListener {

            viewModel.selectedUser.value?.imageUrl?.let { avatarUrl ->
                val action = ProfileFragmentDirections.actionProfileFragmentToDetailAvatarFragment(
                    avatarUrl = avatarUrl
                )
                findNavController().navigate(action)
            }
        }
    }

    // region post callbacks

    override fun commentClick(postId: String)
    {
        val action = ProfileFragmentDirections.actionProfileFragmentToCommentFragment(
            postId = postId
        )
        findNavController().navigate(action)
    }

    override fun imageClick(postWithId: PostWithId)
    {
        val action = ProfileFragmentDirections.actionProfileFragmentToDetailPostFragment(
            post = postWithId.second,
            postId = postWithId.first
        )
        findNavController().navigate(action)
    }

    override fun mentionClick(mention: String)
    {
        if (viewModel.isOwnAccountUsername(mention)) // user  clicked on own profile
        {
            binding.userLayout.showSnackbarGravity(
                message = getString(R.string.you_are_currently_on_your_profile)
            )
        }
        else
        {
            val action = ProfileFragmentDirections.actionProfileFragmentToUserFragment(
                user = User(username = mention),
                loadUserFromDb = true
            )
            findNavController().navigate(action)
        }
    }

    override fun tagClick(tag: String)
    {
        val action = ProfileFragmentDirections.actionProfileFragmentToTagFragment(
            tag = Tag(tag, -1),
        )
        findNavController().navigate(action)
    }

    // endregion
}