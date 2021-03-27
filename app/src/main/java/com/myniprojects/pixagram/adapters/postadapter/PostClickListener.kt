package com.myniprojects.pixagram.adapters.postadapter

data class PostClickListener(
    val likeClick: (String, Boolean) -> Unit = { _, _ -> },
    val commentClick: (String) -> Unit = {},
    val shareClick: (String) -> Unit = {},
    val likeCounterClick: (String) -> Unit = {},
    val profileClick: (String) -> Unit = {},
    val imageClick: (PostWithId) -> Unit = {},
    val tagClick: (String) -> Unit = {},
    val linkClick: (String) -> Unit = {},
    val mentionClick: (String) -> Unit = {},
    val menuReportClick: (String) -> Unit = {}
)


