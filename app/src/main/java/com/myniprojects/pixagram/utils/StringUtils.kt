package com.myniprojects.pixagram.utils

import androidx.lifecycle.MutableLiveData

fun MutableLiveData<String>.trim()
{
    value = value?.trim()
}