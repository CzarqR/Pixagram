package com.myniprojects.pixagram.adapters.conversationadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.ConversationItemBinding
import com.myniprojects.pixagram.model.ConversationItem
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.ext.context
import com.myniprojects.pixagram.utils.ext.getDateTimeFormatFromMillis
import com.myniprojects.pixagram.utils.status.GetStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber


class ConversationViewHolder private constructor(
    private val binding: ConversationItemBinding,
    private val cancelListener: (Int) -> Unit,
    private val imageLoader: ImageLoader

) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(
            parent: ViewGroup,
            cancelListener: (Int) -> Unit,
            imageLoader: ImageLoader
        ): ConversationViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemBinding.inflate(layoutInflater, parent, false)

            return ConversationViewHolder(
                binding,
                cancelListener,
                imageLoader
            )
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main)

    private var userJob: Job? = null
    private var userListenerId: Int = -1


    fun cancelJobs()
    {
        userJob?.cancel()
        cancelListener(userListenerId)
    }


    fun bind(
        conversationItem: ConversationItem,
        actionConversationClick: (User) -> Unit,
        userFlow: (Int, String) -> Flow<GetStatus<User>>,
    )
    {
        cancelJobs()

        userJob = scope.launch {
            userListenerId = FirebaseRepository.userListenerId
            userFlow(userListenerId, conversationItem.userId).collectLatest {
                setUserData(it)
            }
        }

        binding.txtLastMsg.text = conversationItem.lastMessage.textContent
        binding.txtTime.text = conversationItem.lastMessage.time.getDateTimeFormatFromMillis()

        binding.root.setOnClickListener {
            loadedUser?.let { user ->
                actionConversationClick(user)
            }
        }
    }

    private var loadedUser: User? = null

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
            }
            GetStatus.Loading ->
            {
                loadedUser = null
                binding.txtUsername.text = binding.context.getString(R.string.loading_dots)
            }
            is GetStatus.Success ->
            {
                loadedUser = status.data

                with(binding)
                {
                    txtUsername.text = status.data.username

                    val request = ImageRequest.Builder(context)
                        .data(status.data.imageUrl)
                        .target { drawable ->
                            imgAvatar.setImageDrawable(drawable)
                        }
                        .build()

                    imageLoader.enqueue(request)
                }
            }
            GetStatus.Sleep -> Unit
        }
    }
}
