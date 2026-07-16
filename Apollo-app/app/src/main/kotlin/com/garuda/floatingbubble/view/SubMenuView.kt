package com.garuda.floatingbubble.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.garuda.floatingbubble.R
import kotlin.math.abs

enum class SubMenuType {
    CAMERA,
    CLOSE
}

class SubMenuView(
    context: Context,
    val type: SubMenuType
) : FrameLayout(context) {

    var onClicked: (() -> Unit)? = null
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    init {
        setBackgroundResource(R.drawable.bg_bubble_sub)
        if (type == SubMenuType.CLOSE) {
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.awam_danger))
        }

        val ivIcon = ImageView(context).apply {
            val drawableRes = if (type == SubMenuType.CAMERA) R.drawable.ic_camera else R.drawable.ic_close
            setImageResource(drawableRes)
            
            if (type == SubMenuType.CLOSE) {
                imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
            }
            
            isClickable = false
            isFocusable = false
            layoutParams = LayoutParams(
                resources.getDimensionPixelSize(R.dimen.bubble_sub_size) / 2,
                resources.getDimensionPixelSize(R.dimen.bubble_sub_size) / 2
            ).apply {
                gravity = Gravity.CENTER
            }
        }
        addView(ivIcon)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }
            MotionEvent.ACTION_UP -> {
                val deltaX = abs(event.rawX - initialTouchX)
                val deltaY = abs(event.rawY - initialTouchY)
                if (deltaX < touchSlop && deltaY < touchSlop) {
                    onClicked?.invoke()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun animateShow() {
        visibility = View.VISIBLE
        val anim = AnimationUtils.loadAnimation(context, R.anim.bubble_sub_show)
        startAnimation(anim)
    }

    fun animateHide(onComplete: (() -> Unit)? = null) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.bubble_sub_hide)
        anim.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                visibility = View.GONE
                onComplete?.invoke()
            }
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })
        startAnimation(anim)
    }
}
