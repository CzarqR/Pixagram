package com.myniprojects.pixagram.utils

import androidx.lifecycle.MutableLiveData
import java.util.*

fun MutableLiveData<String>.trim()
{
    value = value?.trim()
}

fun String.formatQuery(): String = this.toLowerCase(Locale.getDefault()).trim()