package com.ridecast

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application entry point.
 *
 * Annotated with [@HiltAndroidApp] to trigger Hilt's code generation and initialise
 * the dependency injection container. All other Hilt-injected components depend on
 * the component graph rooted here.
 */
@HiltAndroidApp
class RideCastApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            // DebugTree includes file name, line number, and method name automatically.
            Timber.plant(Timber.DebugTree())
        }
        // Production crash-reporting tree (e.g. Firebase Crashlytics) would be planted here.
    }
}
