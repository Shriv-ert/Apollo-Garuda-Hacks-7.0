package com.garuda.floatingbubble.util

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager

object ScreenFlashUtil {
    fun flashScreen(context: Context, durationMs: Long = 120L) {
        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val flashView = View(context).apply {
                setBackgroundColor(Color.WHITE)
            }

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )

            windowManager.addView(flashView, params)

            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    windowManager.removeView(flashView)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, durationMs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
