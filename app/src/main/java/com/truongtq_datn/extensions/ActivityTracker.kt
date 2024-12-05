package com.truongtq_datn.extensions

import android.app.Activity
import android.app.Application
import android.os.Bundle

class ActivityTracker : Application.ActivityLifecycleCallbacks {
    private var currentActivity: Activity? = null

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
    }

    fun getCurrentActivity(): Activity? {
        return currentActivity
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        currentActivity = activity
    }
}
