package com.garuda.floatingbubble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.garuda.floatingbubble.scanner.FakeScanEngine
import com.garuda.floatingbubble.scanner.ScanResult
import com.garuda.floatingbubble.util.ScreenFlashUtil
import com.garuda.floatingbubble.view.BubbleView
import com.garuda.floatingbubble.view.ResultPanelView
import com.garuda.floatingbubble.view.SubMenuView
import com.garuda.floatingbubble.view.SubMenuType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class FloatingBubbleService : Service() {

    enum class BubbleState {
        IDLE,
        OPENING_MENU,
        MENU_OPEN,
        CLOSING_MENU,
        SCANNING,
        SHOW_RESULT
    }

    private lateinit var windowManager: WindowManager
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var currentState = BubbleState.IDLE
    private var currentAnimator: android.animation.ValueAnimator? = null

    private var bubbleView: BubbleView? = null
    private var subMenuViewCamera: SubMenuView? = null
    private var subMenuViewPaste: SubMenuView? = null
    private var subMenuViewHome: SubMenuView? = null
    private var subMenuViewClose: SubMenuView? = null
    private var resultPanelView: ResultPanelView? = null

    private lateinit var bubbleParams: WindowManager.LayoutParams
    private var subMenuParamsCamera: WindowManager.LayoutParams? = null
    private var subMenuParamsPaste: WindowManager.LayoutParams? = null
    private var subMenuParamsHome: WindowManager.LayoutParams? = null
    private var subMenuParamsClose: WindowManager.LayoutParams? = null

    private var initialBubbleX = 0
    private var initialBubbleY = 0

    private val layoutType: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ACTION_APP_FOREGROUND") {
            hideBubble()
        } else if (intent?.action == "ACTION_APP_BACKGROUND") {
            showBubble()
        }
        return START_STICKY
    }

    private fun hideBubble() {
        if (currentState != BubbleState.IDLE) {
            closeSubMenu()
        }
        bubbleView?.visibility = View.GONE
    }

    private fun showBubble() {
        if (currentState == BubbleState.IDLE) {
            bubbleView?.visibility = View.VISIBLE
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        startForegroundServiceNotification()
        setupSubMenus()
        setupBubbleView()
    }

    private fun setupBubbleView() {
        bubbleView = BubbleView(this).apply {
            onBubbleTouchDown = {
                initialBubbleX = bubbleParams.x
                initialBubbleY = bubbleParams.y
            }

            onBubbleDragged = { totalDeltaX, totalDeltaY ->
                if (currentState != BubbleState.SCANNING && currentState != BubbleState.SHOW_RESULT) {
                    bubbleParams.x = initialBubbleX + totalDeltaX
                    bubbleParams.y = initialBubbleY + totalDeltaY
                    try { windowManager.updateViewLayout(this, bubbleParams) } catch (e: Exception) {}

                    if (currentState == BubbleState.MENU_OPEN || currentState == BubbleState.CLOSING_MENU || currentState == BubbleState.OPENING_MENU) {
                        updateSubMenuPosition()
                    }
                    
                    if (currentState == BubbleState.MENU_OPEN) {
                        closeSubMenu()
                    }
                }
            }

            onBubbleClicked = {
                when (currentState) {
                    BubbleState.IDLE -> openSubMenu()
                    BubbleState.MENU_OPEN -> closeSubMenu()
                    else -> {}
                }
            }
            
            onBubbleOutsideTouched = {
                if (currentState == BubbleState.MENU_OPEN) {
                    closeSubMenu()
                }
            }
        }
        
        bubbleParams = WindowManager.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.bubble_main_size),
            resources.getDimensionPixelSize(R.dimen.bubble_main_size),
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 48
            y = 400
        }

        windowManager.addView(bubbleView, bubbleParams)
    }

    private fun setupSubMenus() {
        val subSize = resources.getDimensionPixelSize(R.dimen.bubble_sub_size)

        subMenuViewCamera = SubMenuView(this, SubMenuType.CAMERA).apply {
            visibility = View.GONE
            onClicked = { if (currentState == BubbleState.MENU_OPEN) startScanningProcess(null) }
        }
        subMenuParamsCamera = WindowManager.LayoutParams(subSize, subSize, layoutType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        subMenuViewPaste = SubMenuView(this, SubMenuType.PASTE).apply {
            visibility = View.GONE
            onClicked = { if (currentState == BubbleState.MENU_OPEN) startScanningFromClipboard() }
        }
        subMenuParamsPaste = WindowManager.LayoutParams(subSize, subSize, layoutType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        subMenuViewHome = SubMenuView(this, SubMenuType.HOME).apply {
            visibility = View.GONE
            onClicked = { 
                if (currentState == BubbleState.MENU_OPEN) {
                    val intent = Intent(this@FloatingBubbleService, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    startActivity(intent)
                    closeSubMenu()
                }
            }
        }
        subMenuParamsHome = WindowManager.LayoutParams(subSize, subSize, layoutType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        subMenuViewClose = SubMenuView(this, SubMenuType.CLOSE).apply {
            visibility = View.GONE
            onClicked = { if (currentState == BubbleState.MENU_OPEN) stopSelf() }
        }
        subMenuParamsClose = WindowManager.LayoutParams(subSize, subSize, layoutType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            windowManager.addView(subMenuViewCamera, subMenuParamsCamera)
            windowManager.addView(subMenuViewPaste, subMenuParamsPaste)
            windowManager.addView(subMenuViewHome, subMenuParamsHome)
            windowManager.addView(subMenuViewClose, subMenuParamsClose)
        } catch (e: Exception) {}
    }

    private fun getSubMenuCoordinates(): List<Pair<Int, Int>> {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val screenCenterX = displayMetrics.widthPixels / 2f
        val screenCenterY = displayMetrics.heightPixels / 2f

        val mainSize = resources.getDimensionPixelSize(R.dimen.bubble_main_size)
        val subSize = resources.getDimensionPixelSize(R.dimen.bubble_sub_size)
        val margin = resources.getDimensionPixelSize(R.dimen.bubble_sub_margin_bottom)

        val bubbleCenterX = bubbleParams.x + mainSize / 2f
        val bubbleCenterY = bubbleParams.y + mainSize / 2f

        val dirX = screenCenterX - bubbleCenterX
        val dirY = screenCenterY - bubbleCenterY
        
        val baseAngle = kotlin.math.atan2(dirY.toDouble(), dirX.toDouble())
        val angleSpread = Math.PI / 6.0 
        val radius = (mainSize / 2f) + (subSize / 2f) + (margin * 3.5f)

        val angleCamera = baseAngle + angleSpread * 1.5
        val anglePaste = baseAngle + angleSpread * 0.5
        val angleHome = baseAngle - angleSpread * 0.5
        val angleClose = baseAngle - angleSpread * 1.5

        val cameraCenterX = bubbleCenterX + radius * kotlin.math.cos(angleCamera).toFloat()
        val cameraCenterY = bubbleCenterY + radius * kotlin.math.sin(angleCamera).toFloat()

        val pasteCenterX = bubbleCenterX + radius * kotlin.math.cos(anglePaste).toFloat()
        val pasteCenterY = bubbleCenterY + radius * kotlin.math.sin(anglePaste).toFloat()
        
        val homeCenterX = bubbleCenterX + radius * kotlin.math.cos(angleHome).toFloat()
        val homeCenterY = bubbleCenterY + radius * kotlin.math.sin(angleHome).toFloat()

        val closeCenterX = bubbleCenterX + radius * kotlin.math.cos(angleClose).toFloat()
        val closeCenterY = bubbleCenterY + radius * kotlin.math.sin(angleClose).toFloat()

        val cameraX = (cameraCenterX - subSize / 2f).toInt().coerceIn(0, displayMetrics.widthPixels - subSize)
        val cameraY = (cameraCenterY - subSize / 2f).toInt().coerceIn(0, displayMetrics.heightPixels - subSize)

        val pasteX = (pasteCenterX - subSize / 2f).toInt().coerceIn(0, displayMetrics.widthPixels - subSize)
        val pasteY = (pasteCenterY - subSize / 2f).toInt().coerceIn(0, displayMetrics.heightPixels - subSize)
        
        val homeX = (homeCenterX - subSize / 2f).toInt().coerceIn(0, displayMetrics.widthPixels - subSize)
        val homeY = (homeCenterY - subSize / 2f).toInt().coerceIn(0, displayMetrics.heightPixels - subSize)

        val closeX = (closeCenterX - subSize / 2f).toInt().coerceIn(0, displayMetrics.widthPixels - subSize)
        val closeY = (closeCenterY - subSize / 2f).toInt().coerceIn(0, displayMetrics.heightPixels - subSize)

        return listOf(Pair(cameraX, cameraY), Pair(pasteX, pasteY), Pair(homeX, homeY), Pair(closeX, closeY))
    }

    private fun openSubMenu() {
        if (currentState != BubbleState.IDLE) return
        currentState = BubbleState.OPENING_MENU

        val coords = getSubMenuCoordinates()
        val cameraCoords = coords[0]
        val pasteCoords = coords[1]
        val homeCoords = coords[2]
        val closeCoords = coords[3]

        val mainSize = resources.getDimensionPixelSize(R.dimen.bubble_main_size)
        val subSize = resources.getDimensionPixelSize(R.dimen.bubble_sub_size)
        val bubbleCenterX = bubbleParams.x + mainSize / 2f
        val bubbleCenterY = bubbleParams.y + mainSize / 2f
        val startX = (bubbleCenterX - subSize / 2f).toInt()
        val startY = (bubbleCenterY - subSize / 2f).toInt()

        subMenuViewCamera?.visibility = View.VISIBLE
        subMenuViewPaste?.visibility = View.VISIBLE
        subMenuViewHome?.visibility = View.VISIBLE
        subMenuViewClose?.visibility = View.VISIBLE

        currentAnimator?.cancel()
        currentAnimator = android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 250
            interpolator = android.view.animation.OvershootInterpolator()
            addUpdateListener { anim ->
                val fraction = anim.animatedValue as Float
                subMenuParamsCamera?.x = (startX + (cameraCoords.first - startX) * fraction).toInt()
                subMenuParamsCamera?.y = (startY + (cameraCoords.second - startY) * fraction).toInt()
                subMenuParamsPaste?.x = (startX + (pasteCoords.first - startX) * fraction).toInt()
                subMenuParamsPaste?.y = (startY + (pasteCoords.second - startY) * fraction).toInt()
                subMenuParamsHome?.x = (startX + (homeCoords.first - startX) * fraction).toInt()
                subMenuParamsHome?.y = (startY + (homeCoords.second - startY) * fraction).toInt()
                subMenuParamsClose?.x = (startX + (closeCoords.first - startX) * fraction).toInt()
                subMenuParamsClose?.y = (startY + (closeCoords.second - startY) * fraction).toInt()
                
                try { subMenuViewCamera?.let { windowManager.updateViewLayout(it, subMenuParamsCamera) } } catch (e: Exception) {}
                try { subMenuViewPaste?.let { windowManager.updateViewLayout(it, subMenuParamsPaste) } } catch (e: Exception) {}
                try { subMenuViewHome?.let { windowManager.updateViewLayout(it, subMenuParamsHome) } } catch (e: Exception) {}
                try { subMenuViewClose?.let { windowManager.updateViewLayout(it, subMenuParamsClose) } } catch (e: Exception) {}
            }
            start()
        }

        subMenuViewCamera?.animateShow()
        subMenuViewPaste?.animateShow()
        subMenuViewHome?.animateShow()
        subMenuViewClose?.animateShow()

        bubbleView?.postDelayed({
            if (currentState == BubbleState.OPENING_MENU) {
                currentState = BubbleState.MENU_OPEN
            }
        }, 160L)
    }

    private fun updateSubMenuPosition() {
        val coords = getSubMenuCoordinates()
        val cameraCoords = coords[0]
        val pasteCoords = coords[1]
        val homeCoords = coords[2]
        val closeCoords = coords[3]

        subMenuViewCamera?.let { sub ->
            subMenuParamsCamera?.let { params ->
                params.x = cameraCoords.first
                params.y = cameraCoords.second
                try { windowManager.updateViewLayout(sub, params) } catch (e: Exception) {}
            }
        }

        subMenuViewPaste?.let { sub ->
            subMenuParamsPaste?.let { params ->
                params.x = pasteCoords.first
                params.y = pasteCoords.second
                try { windowManager.updateViewLayout(sub, params) } catch (e: Exception) {}
            }
        }
        
        subMenuViewHome?.let { sub ->
            subMenuParamsHome?.let { params ->
                params.x = homeCoords.first
                params.y = homeCoords.second
                try { windowManager.updateViewLayout(sub, params) } catch (e: Exception) {}
            }
        }

        subMenuViewClose?.let { sub ->
            subMenuParamsClose?.let { params ->
                params.x = closeCoords.first
                params.y = closeCoords.second
                try { windowManager.updateViewLayout(sub, params) } catch (e: Exception) {}
            }
        }
    }

    private fun closeSubMenu(onClosed: (() -> Unit)? = null) {
        if (currentState != BubbleState.MENU_OPEN) {
            onClosed?.invoke()
            return
        }
        currentState = BubbleState.CLOSING_MENU

        val mainSize = resources.getDimensionPixelSize(R.dimen.bubble_main_size)
        val subSize = resources.getDimensionPixelSize(R.dimen.bubble_sub_size)
        val endX = (bubbleParams.x + mainSize / 2f - subSize / 2f).toInt()
        val endY = (bubbleParams.y + mainSize / 2f - subSize / 2f).toInt()

        val startCamX = subMenuParamsCamera?.x ?: 0
        val startCamY = subMenuParamsCamera?.y ?: 0
        val startPasteX = subMenuParamsPaste?.x ?: 0
        val startPasteY = subMenuParamsPaste?.y ?: 0
        val startHomeX = subMenuParamsHome?.x ?: 0
        val startHomeY = subMenuParamsHome?.y ?: 0
        val startCloseX = subMenuParamsClose?.x ?: 0
        val startCloseY = subMenuParamsClose?.y ?: 0

        currentAnimator?.cancel()
        currentAnimator = android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 150
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
            addUpdateListener { anim ->
                val fraction = anim.animatedValue as Float
                subMenuParamsCamera?.x = (startCamX + (endX - startCamX) * fraction).toInt()
                subMenuParamsCamera?.y = (startCamY + (endY - startCamY) * fraction).toInt()
                subMenuParamsPaste?.x = (startPasteX + (endX - startPasteX) * fraction).toInt()
                subMenuParamsPaste?.y = (startPasteY + (endY - startPasteY) * fraction).toInt()
                subMenuParamsHome?.x = (startHomeX + (endX - startHomeX) * fraction).toInt()
                subMenuParamsHome?.y = (startHomeY + (endY - startHomeY) * fraction).toInt()
                subMenuParamsClose?.x = (startCloseX + (endX - startCloseX) * fraction).toInt()
                subMenuParamsClose?.y = (startCloseY + (endY - startCloseY) * fraction).toInt()
                
                try { subMenuViewCamera?.let { windowManager.updateViewLayout(it, subMenuParamsCamera) } } catch (e: Exception) {}
                try { subMenuViewPaste?.let { windowManager.updateViewLayout(it, subMenuParamsPaste) } } catch (e: Exception) {}
                try { subMenuViewHome?.let { windowManager.updateViewLayout(it, subMenuParamsHome) } } catch (e: Exception) {}
                try { subMenuViewClose?.let { windowManager.updateViewLayout(it, subMenuParamsClose) } } catch (e: Exception) {}
            }
            start()
        }

        subMenuViewCamera?.animateHide()
        subMenuViewPaste?.animateHide()
        subMenuViewHome?.animateHide()
        subMenuViewClose?.animateHide()

        bubbleView?.postDelayed({
            try { subMenuViewCamera?.visibility = View.GONE } catch (e: Exception) {}
            try { subMenuViewPaste?.visibility = View.GONE } catch (e: Exception) {}
            try { subMenuViewHome?.visibility = View.GONE } catch (e: Exception) {}
            try { subMenuViewClose?.visibility = View.GONE } catch (e: Exception) {}
            
            if (currentState == BubbleState.CLOSING_MENU) {
                currentState = BubbleState.IDLE
            }
            onClosed?.invoke()
        }, 160L)
    }

    private fun startScanningFromClipboard() {
        val focusParams = WindowManager.LayoutParams(
            1, 1,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        val dummyView = View(this)
        windowManager.addView(dummyView, focusParams)

        dummyView.postDelayed({
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val copiedText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()

            windowManager.removeView(dummyView)

            if (copiedText.isNullOrEmpty()) {
                android.widget.Toast.makeText(this, "Clipboard kosong", android.widget.Toast.LENGTH_SHORT).show()
                closeSubMenu()
                return@postDelayed
            }

            startDirectPasteProcess(copiedText)
        }, 50)
    }

    private fun startDirectPasteProcess(textToScan: String) {
        if (currentState == BubbleState.SCANNING || currentState == BubbleState.SHOW_RESULT) return
        currentState = BubbleState.SCANNING

        try { subMenuViewCamera?.visibility = View.GONE } catch (e: Exception) {}
        try { subMenuViewPaste?.visibility = View.GONE } catch (e: Exception) {}
        try { subMenuViewHome?.visibility = View.GONE } catch (e: Exception) {}
        try { subMenuViewClose?.visibility = View.GONE } catch (e: Exception) {}

        serviceScope.launch {
            val scanResult = FakeScanEngine.performInstantFakeScan(textToScan)
            showResultPanel(scanResult)
        }
    }

    private fun startScanningProcess(textToScan: String?) {
        if (currentState == BubbleState.SCANNING || currentState == BubbleState.SHOW_RESULT) return
        
        currentState = BubbleState.SCANNING

        try { subMenuViewCamera?.visibility = View.GONE } catch (e: Exception) {}
        try { subMenuViewPaste?.visibility = View.GONE } catch (e: Exception) {}
        try { subMenuViewHome?.visibility = View.GONE } catch (e: Exception) {}
        try { subMenuViewClose?.visibility = View.GONE } catch (e: Exception) {}

        ScreenFlashUtil.flashScreen(this@FloatingBubbleService, 120L)

        bubbleView?.setScanningState(true)

        serviceScope.launch {
            val scanResult = FakeScanEngine.performFakeScan(textToScan)
            showResultPanel(scanResult)
        }
    }

    private fun showResultPanel(result: ScanResult) {
        currentState = BubbleState.SHOW_RESULT

        bubbleView?.setScanningState(false)
        bubbleView?.visibility = View.GONE

        val panelParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        var initialPanelX = 0
        var initialPanelY = 0

        resultPanelView = ResultPanelView(this).apply {
            bindScanResult(result)
            onCloseClicked = {
                closeResultPanel()
            }
            onPanelTouchDown = {
                initialPanelX = panelParams.x
                initialPanelY = panelParams.y
            }
            onPanelDragged = { deltaX, deltaY ->
                panelParams.x = initialPanelX + deltaX
                panelParams.y = initialPanelY + deltaY
                windowManager.updateViewLayout(this, panelParams)
            }
            onPanelSizeChanged = {
                // Ensure LayoutParams is WRAP_CONTENT to wrap the newly visible/hidden elements
                panelParams.width = WindowManager.LayoutParams.WRAP_CONTENT
                panelParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                windowManager.updateViewLayout(this, panelParams)
            }
        }

        windowManager.addView(resultPanelView, panelParams)
        resultPanelView?.animateSlideUp()
    }

    private fun closeResultPanel() {
        resultPanelView?.animateSlideDown {
            resultPanelView?.let {
                try {
                    windowManager.removeView(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            resultPanelView = null

            bubbleView?.visibility = View.VISIBLE
            currentState = BubbleState.IDLE
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "floating_bubble_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "AWAM Verification Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("AWAM - Agen Verifikasi Aktif")
            .setContentText("Ketuk Awam melayang untuk memindai penipuan di layar Anda.")
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        currentAnimator?.cancel()

        bubbleView?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
        subMenuViewCamera?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
        subMenuViewPaste?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
        subMenuViewHome?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
        subMenuViewClose?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
        resultPanelView?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
    }
}
