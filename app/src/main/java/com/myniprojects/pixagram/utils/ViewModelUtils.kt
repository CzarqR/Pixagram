package com.myniprojects.pixagram.utils

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

val AndroidViewModel.context: Context
    get() = getApplication<Application>().applicationContext