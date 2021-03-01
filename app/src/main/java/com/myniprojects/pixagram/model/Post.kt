package com.myniprojects.pixagram.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val desc: String = "",
    val imageUrl: String = "",
    val owner: String = "",
    val time: Long = 0L,
) : Parcelable