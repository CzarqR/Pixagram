package com.myniprojects.pixagram.adapters.imageadapter

import android.net.Uri

data class Image(
    val uri: Uri,
    var isSelected: Boolean
)
