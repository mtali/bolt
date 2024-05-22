package org.mtali.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.mtali.BuildConfig
import timber.log.Timber

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}