package com.garuda.floatingbubble

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle

class AwamApplication : Application(), Application.ActivityLifecycleCallbacks {
    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M || android.provider.Settings.canDrawOverlays(this)) {
                val intent = Intent(this, FloatingBubbleService::class.java).apply {
                    action = "ACTION_APP_FOREGROUND"
                }
                startService(intent)
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val isBubbleEnabled = prefs.getBoolean("bubble_enabled", true)
            
            if (isBubbleEnabled && (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M || android.provider.Settings.canDrawOverlays(this))) {
                val intent = Intent(this, FloatingBubbleService::class.java).apply {
                    action = "ACTION_APP_BACKGROUND"
                }
                startService(intent)
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
