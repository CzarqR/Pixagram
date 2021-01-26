package com.myniprojects.pixagram.ui

import android.app.Application
import com.myniprojects.pixagram.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import jdenticon.Jdenticon
import timber.log.Timber

@HiltAndroidApp
class BaseApp : Application()
{
    override fun onCreate()
    {
        super.onCreate()
        if (BuildConfig.DEBUG)
        {
            Timber.plant(Timber.DebugTree())

            Timber.d("Before")
            val x = Jdenticon.toSvg("Czarqasf123R", 512)
            Timber.d(x)
            Timber.d("After")
        }
    }
}
