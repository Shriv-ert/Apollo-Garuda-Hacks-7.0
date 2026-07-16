package com.garuda.floatingbubble.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.garuda.floatingbubble.R
import kotlin.math.abs

class BubbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ivLogo: ImageView
    private val progressBar: ProgressBar
    private var isScanning = false

    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    var onBubbleTouchDown: (() -> Unit)? = null
    var onBubbleDragged: ((totalDeltaX: Int, totalDeltaY: Int) -> Unit)? = null
    var onBubbleClicked: (() -> Unit)? = null

    init {
        setBackgroundResource(R.drawable.bg_bubble_main)

        ivLogo = ImageView(context).apply {
            setImageResource(R.drawable.ic_awam_logo)
            isClickable = false
            isFocusable = false
            layoutParams = LayoutParams(
                resources.getDimensionPixelSize(R.dimen.bubble_main_size) / 2,
                resources.getDimensionPixelSize(R.dimen.bubble_main_size) / 2
            ).apply {
                gravity = Gravity.CENTER
            }
        }
        addView(ivLogo)

        progressBar = ProgressBar(context).apply {
            isIndeterminate = true
            indeterminateTintList = ColorStateList.valueOf(Color.WHITE)
            visibility = View.GONE
            isClickable = false
            isFocusable = false
            layoutParams = LayoutParams(
                resources.getDimensionPixelSize(R.dimen.bubble_main_size) - 16,
                resources.getDimensionPixelSize(R.dimen.bubble_main_size) - 16
            ).apply {
                gravity = Gravity.CENTER
            }
        }
        addView(progressBar)
    }

    fun setScanningState(scanning: Boolean) {
        if (isScanning == scanning) return
        isScanning = scanning
        if (scanning) {
            ivLogo.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            setBackgroundResource(R.drawable.bg_bubble_sub)
        } else {
            progressBar.visibility = View.GONE
            ivLogo.visibility = View.VISIBLE
            setBackgroundResource(R.drawable.bg_bubble_main)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isScanning) {
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                alpha = 1.0f
                onBubbleTouchDown?.invoke()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // Total perpindahan dari posisi awal sentuhan jari
                val totalDeltaX = (event.rawX - initialTouchX).toInt()
                val totalDeltaY = (event.rawY - initialTouchY).toInt()
                if (abs(totalDeltaX) > touchSlop || abs(totalDeltaY) > touchSlop) {
                    onBubbleDragged?.invoke(totalDeltaX, totalDeltaY)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                val totalDeltaX = abs(event.rawX - initialTouchX)
                val totalDeltaY = abs(event.rawY - initialTouchY)
                if (totalDeltaX < touchSlop && totalDeltaY < touchSlop) {
                    onBubbleClicked?.invoke()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
