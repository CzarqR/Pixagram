package com.myniprojects.pixagram.adapters.postadapter

/**
 * [PostClickListener] is an interface that handle
 * every interaction that user can do with post item
 */
interface PostClickListener
{
    fun likeClick(postId: String, status: Boolean)

    fun commentClick(postId: String)

    fun shareClick(postId: String)

    fun likeCounterClick(postId: String)

    fun profileClick(postOwner: String)

    fun imageClick(postWithId: PostWithId)

    fun tagClick(tag: String)

    fun linkClick(link: String)

    fun mentionClick(mention: String)

    fun menuReportClick(postId: String)

    fun menuEditClick(post: PostWithId)
}


