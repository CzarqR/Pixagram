package com.myniprojects.pixagram.adapters.postadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.bumptech.glide.RequestManager
import com.google.android.material.button.MaterialButton
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.PostItemBinding
import com.myniprojects.pixagram.model.LikeStatus
import com.myniprojects.pixagram.model.Post
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.repository.FirebaseRepository
import com.myniprojects.pixagram.utils.ext.context
import com.myniprojects.pixagram.utils.ext.exhaustive
import com.myniprojects.pixagram.utils.ext.formatWithSpaces
import com.myniprojects.pixagram.utils.ext.getDateTimeFormat
import com.myniprojects.pixagram.utils.status.GetStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * first - id of post
 * second - post value
 */
typealias PostWithId = Pair<String, Post>

class PostViewHolder private constructor(
    private val binding: PostItemBinding,
    private val cancelListeners: (Int, Int, Int) -> Unit
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup, cancelListeners: (Int, Int, Int) -> Unit): PostViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = PostItemBinding.inflate(layoutInflater, parent, false)

            return PostViewHolder(
                binding,
                cancelListeners
            ).apply {
                baseDescLengthLines = binding.context.resources.getInteger(R.integer.max_lines_post_desc)

                binding.txtDesc.setOnClickListener {
                    isCollapsed = !isCollapsed
                }

                binding.butMore.setOnClickListener {
                    showPopupMenu(it)
                }
            }
        }
    }

    private fun setOnClickListeners(
        post: PostWithId,
        postClickListener: PostClickListener
    )
    {
        with(binding)
        {
            butLike.setOnClickListener {
                postClickListener.likeClick(post.first, !isPostLiked)
            }

            butShare.setOnClickListener {
                postClickListener.shareClick(post.first)
            }

            butComment.setOnClickListener {
                postClickListener.commentClick(post.first)
            }

            txtComments.setOnClickListener {
                postClickListener.commentClick(post.first)
            }

            linLayLikeCounter.setOnClickListener {
                postClickListener.likeCounterClick(post.first)
            }

            imgAvatar.setOnClickListener {
                postClickListener.profileClick(post.second.owner)
            }

            txtOwner.setOnClickListener {
                postClickListener.profileClick(post.second.owner)
            }

            imgPost.setOnClickListener {
                postClickListener.imageClick(post)
            }

            txtDesc.setOnHashtagClickListener { _, text -> postClickListener.tagClick(text.toString()) }
            txtDesc.setOnHyperlinkClickListener { _, text -> postClickListener.linkClick(text.toString()) }
            txtDesc.setOnMentionClickListener { _, text -> postClickListener.mentionClick(text.toString()) }
        }
    }

    /**
     * when [PostViewHolder] is created
     * [baseDescLengthLines] will keep the max lines
     * of description TextView
     */
    private lateinit var imageLoader: ImageLoader

    private var baseDescLengthLines = -1
    private var isCollapsed: Boolean = true
        set(value)
        {
            field = value
            binding.txtDesc.maxLines = if (value)
            {
                baseDescLengthLines
            }
            else
            {
                Int.MAX_VALUE
            }
        }

    private var isPostLiked = false
        set(value)
        {
            field = value

            if (value) // Post is liked
            {
                with(binding)
                {
                    (butLike as MaterialButton).apply {
                        icon = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_baseline_favorite_24
                        )
                        setIconTintResource(R.color.red_on_surface)
                    }
                }
            }
            else // post is not liked
            {
                with(binding)
                {
                    (butLike as MaterialButton).apply {
                        icon = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_outline_favorite_border_24
                        )
                        setIconTintResource(R.color.button_on_surface)
                    }

                }
            }
        }


    private val scope = CoroutineScope(Dispatchers.Main)

    private var userJob: Job? = null
    private var userListenerId: Int = -1

    private var likeJob: Job? = null
    private var likeListenerId: Int = -1

    private var commentCounterJob: Job? = null
    private var commentCounterListenerId: Int = -1

    fun cancelJobs()
    {
        userJob?.cancel()
        likeJob?.cancel()

        cancelListeners(userListenerId, likeListenerId, commentCounterListenerId)
    }

    private lateinit var postClickListener: PostClickListener
    private lateinit var post: PostWithId

    fun bind(
        post: PostWithId,
        glide: RequestManager,
        imageLoader: ImageLoader,
        postClickListener: PostClickListener,
        userFlow: (Int, String) -> Flow<GetStatus<User>>,
        likeFlow: (Int, String) -> Flow<GetStatus<LikeStatus>>,
        commentCounterFlow: (Int, String) -> Flow<GetStatus<Long>>,
    )
    {
        this.postClickListener = postClickListener
        this.post = post
        isCollapsed = true

        this.imageLoader = imageLoader
        cancelJobs()

        userJob = scope.launch {
            userListenerId = FirebaseRepository.userListenerId
            userFlow(userListenerId, post.second.owner).collectLatest {
                setUserData(it)
            }
        }

        likeJob = scope.launch {
            likeListenerId = FirebaseRepository.likeListenerId
            likeFlow(likeListenerId, post.first).collectLatest {
                setLikeStatus(it)
            }
        }

        commentCounterJob = scope.launch {
            commentCounterListenerId = FirebaseRepository.commentCounterListenerId
            commentCounterFlow(commentCounterListenerId, post.first).collectLatest {
                setCommentStatus(it)
            }
        }

        with(binding)
        {
            glide
                .load(post.second.imageUrl)
                .into(imgPost)

            txtDesc.text = post.second.desc

            txtTime.text = post.second.time.getDateTimeFormat()
        }

        setOnClickListeners(post, postClickListener)
    }

    private fun showPopupMenu(view: View)
    {
        val popupMenu = PopupMenu(view.context, view)

        popupMenu.inflate(R.menu.menu_post_dropdown_collapse)

        // desc can be collapsed
        if (binding.txtDesc.lineCount > baseDescLengthLines)
        {
            popupMenu.menu.findItem(R.id.mi_collapse).title = binding.context.getString(
                if (isCollapsed) R.string.show_description
                else R.string.collapse_description
            )
        }
        else // desc cannot be collapsed (too short)
        {
            popupMenu.menu.findItem(R.id.mi_collapse).isVisible = false
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->

            return@setOnMenuItemClickListener when (menuItem.itemId)
            {
                R.id.mi_report ->
                {
                    Timber.d("Report")
                    postClickListener.menuReportClick(post.first)
                    true
                }
                R.id.mi_collapse ->
                {
                    Timber.d("Show/Hide")
                    isCollapsed = !isCollapsed
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun setLikeStatus(status: GetStatus<LikeStatus>)
    {
        Timber.d("Collected $status")
        when (status)
        {
            GetStatus.Sleep -> Unit
            is GetStatus.Failed ->
            {

            }
            GetStatus.Loading ->
            {
                binding.txtLikesCounter.text = binding.context.getString(R.string.loading_dots)
            }
            is GetStatus.Success ->
            {
                isPostLiked = status.data.isPostLikeByLoggedUser
                binding.txtLikesCounter.text = status.data.likeCounter.toString()
            }
        }.exhaustive
    }


    private fun setCommentStatus(commentStatus: GetStatus<Long>)
    {
        when (commentStatus)
        {
            is GetStatus.Failed ->
            {

            }
            GetStatus.Loading ->
            {

            }
            is GetStatus.Success ->
            {
                binding.txtComments.text = binding.context.getString(
                    R.string.comments_format,
                    commentStatus.data.formatWithSpaces()
                )
            }
        }
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
            }
            GetStatus.Loading ->
            {
                binding.txtOwner.text = binding.context.getString(R.string.loading_dots)
            }
            is GetStatus.Success ->
            {
                with(binding)
                {
                    txtOwner.text = status.data.username

                    val request = coil.request.ImageRequest.Builder(context)
                        .data(status.data.imageUrl)
                        .target { drawable ->
                            imgAvatar.setImageDrawable(drawable)
                        }
                        .build()

                    imageLoader.enqueue(request)
                }
            }
        }
    }
}
