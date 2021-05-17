package com.myniprojects.pixagram.adapters.useradapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.UserItemBinding
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.ext.context
import com.myniprojects.pixagram.utils.status.GetStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class UserViewHolderById private constructor(
    private val binding: UserItemBinding,
    private val cancelListeners: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(
            parent: ViewGroup,
            cancelListeners: (Int) -> Unit,
            imageLoader: ImageLoader
        ): UserViewHolderById
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = UserItemBinding.inflate(layoutInflater, parent, false)

            return UserViewHolderById(
                binding,
                cancelListeners,
            ).apply {
                this.imageLoader = imageLoader
            }
        }
    }

    private lateinit var imageLoader: ImageLoader


    private val scope = CoroutineScope(Dispatchers.Main)
    private var userJob: Job? = null
    private var userListenerId: Int = -1

    fun cancelJobs()
    {
        userJob?.cancel()
        cancelListeners(userListenerId)
    }

    private lateinit var userClick: (User) -> Unit

    fun bind(
        userId: String,
        userFlow: (Int, String) -> Flow<GetStatus<User>>,
        userClick: (User) -> Unit
    )
    {
        cancelJobs()

        userJob = scope.launch {
            userListenerId = FirebaseRepository.userListenerId
            userFlow(userListenerId, userId).collectLatest {
                setUserData(it)
            }
        }
        this.userClick = userClick

    }

    private fun setUserData(
        status: GetStatus<User>,
    )
    {
        when (status)
        {
            is GetStatus.Failed ->
            {
                Timber.d("Failed to load user data")
                binding.imgAvatar.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.context,
                        R.drawable.ic_outline_account_circle_24
                    )
                )
                binding.txtUsername.text = binding.context.getString(R.string.error)
                binding.txtFullName.text = ""
            }
            GetStatus.Loading ->
            {
                binding.txtUsername.text = binding.context.getString(R.string.loading_dots)
                binding.txtFullName.text = ""
            }
            is GetStatus.Success ->
            {
                with(binding)
                {
                    val request = ImageRequest.Builder(context)
                        .data(status.data.imageUrl)
                        .target { drawable ->
                            imgAvatar.setImageDrawable(drawable)
                        }
                        .build()

                    imageLoader.enqueue(request)

                    binding.txtUsername.text = status.data.username
                    binding.txtFullName.text = status.data.fullName
                }
            }
            GetStatus.Sleep -> Unit
        }
    }
}
