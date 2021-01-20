package com.myniprojects.pixagram.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tag(
    val title: String = "",
    val count: Long = 0,
) : Parcelable