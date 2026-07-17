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

    private var bubbleView: BubbleView? = null
    private var subMenuViewCamera: SubMenuView? = null
    private var subMenuViewClose: SubMenuView? = null
    private var resultPanelView: ResultPanelView? = null

    private lateinit var bubbleParams: WindowManager.LayoutParams
    private var subMenuParamsCamera: WindowManager.LayoutParams? = null
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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        setupBubbleView()
    }

    private fun setupBubbleView() {
        val bubbleSize = resources.getDimensionPixelSize(R.dimen.bubble_main_size)
        bubbleParams = WindowManager.LayoutParams(
            bubbleSize,
            bubbleSize,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 48
            y = 400
        }

        bubbleView = BubbleView(this).apply {
            onBubbleTouchDown = {
                // Simpan posisi awal bubble saat jari mulai menyentuh
                initialBubbleX = bubbleParams.x
                initialBubbleY = bubbleParams.y
            }

            onBubbleDragged = { totalDeltaX, totalDeltaY ->
                if (currentState != BubbleState.SCANNING && currentState != BubbleState.SHOW_RESULT) {
                    // Posisi baru = Posisi awal sentuhan + total geseran
                    bubbleParams.x = initialBubbleX + totalDeltaX
                    bubbleParams.y = initialBubbleY + totalDeltaY
                    windowManager.updateViewLayout(this, bubbleParams)

                    // Jika sub-menu sedang terbuka atau tertutup, sinkronisasi posisinya agar tetap menempel
                    if (currentState == BubbleState.MENU_OPEN || currentState == BubbleState.CLOSING_MENU || currentState == BubbleState.OPENING_MENU) {
                        updateSubMenuPosition()
                    }
                    
                    // Sembunyikan otomatis jika awalnya terbuka
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

        windowManager.addView(bubbleView, bubbleParams)
    }

    private fun getSubMenuCoordinates(): Pair<Pair<Int, Int>, Pair<Int, Int>> {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val screenCenterX = displayMetrics.widthPixels / 2f
        val screenCenterY = displayMetrics.heightPixels / 2f

        val mainSize = resources.getDimensionPixelSize(R.dimen.bubble_main_size)
        val subSize = resources.getDimensionPixelSize(R.dimen.bubble_sub_size)
        val margin = resources.getDimensionPixelSize(R.dimen.bubble_sub_margin_bottom)

        val bubbleCenterX = bubbleParams.x + mainSize / 2f
        val bubbleCenterY = bubbleParams.y + mainSize / 2f

        // Vektor dari pusat gelembung menuju ke pusat layar (titik pusat)
        val dirX = screenCenterX - bubbleCenterX
        val dirY = screenCenterY - bubbleCenterY
        val length = sqrt(dirX * dirX + dirY * dirY)

        // Jarak pusat-ke-pusat antara gelembung utama dan pusat grup sub-menu
        val distance = (mainSize / 2f) + (subSize / 2f) + margin

        val (unitX, unitY) = if (length > 0f) {
            Pair(dirX / length, dirY / length)
        } else {
            Pair(0f, -1f) // Fallback pointing up if exactly in center
        }

        // Pusat grup sub-menu
        val groupCenterX = bubbleCenterX + unitX * distance
        val groupCenterY = bubbleCenterY + unitY * distance

        // Vektor tegak lurus (perpendicular)
        val perpX = -unitY
        val perpY = unitX

        // Jarak geser dari pusat grup ke masing-masing sub-menu
        val offsetDistance = (subSize / 2f) + (margin / 2f)

        // Koordinat pusat Camera (x ke posisi positif)
        val cameraCenterX = groupCenterX + perpX * offsetDistance
        val cameraCenterY = groupCenterY + perpY * offsetDistance

        // Koordinat pusat Close X (x ke posisi negatif)
        val closeCenterX = groupCenterX - perpX * offsetDistance
        val closeCenterY = groupCenterY - perpY * offsetDistance

        // Koordinat top-left untuk WindowManager
        val cameraX = (cameraCenterX - subSize / 2f).toInt().coerceIn(0, displayMetrics.widthPixels - subSize)
        val cameraY = (cameraCenterY - subSize / 2f).toInt().coerceIn(0, displayMetrics.heightPixels - subSize)

        val closeX = (closeCenterX - subSize / 2f).toInt().coerceIn(0, displayMetrics.widthPixels - subSize)
        val closeY = (closeCenterY - subSize / 2f).toInt().coerceIn(0, displayMetrics.heightPixels - subSize)

        return Pair(Pair(cameraX, cameraY), Pair(closeX, closeY))
    }

    private fun openSubMenu() {
        if (currentState != BubbleState.IDLE) return
        currentState = BubbleState.OPENING_MENU

        val subSize = resources.getDimensionPixelSize(R.dimen.bubble_sub_size)
        val (cameraCoords, closeCoords) = getSubMenuCoordinates()

        // 1. Camera View
        subMenuParamsCamera = WindowManager.LayoutParams(
            subSize,
            subSize,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = cameraCoords.first
            y = cameraCoords.second
        }

        subMenuViewCamera = SubMenuView(this, SubMenuType.CAMERA).apply {
            onClicked = {
                startScanningProcess()
            }
        }

        // 2. Close View
        subMenuParamsClose = WindowManager.LayoutParams(
            subSize,
            subSize,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = closeCoords.first
            y = closeCoords.second
        }

        subMenuViewClose = SubMenuView(this, SubMenuType.CLOSE).apply {
            onClicked = {
                stopSelf()
            }
        }

        windowManager.addView(subMenuViewCamera, subMenuParamsCamera)
        windowManager.addView(subMenuViewClose, subMenuParamsClose)

        val mainSize = resources.getDimensionPixelSize(R.dimen.bubble_main_size)
        val bubbleCenterX = bubbleParams.x + mainSize / 2f
        val bubbleCenterY = bubbleParams.y + mainSize / 2f

        val startX = (bubbleCenterX - subSize / 2f).toInt()
        val startY = (bubbleCenterY - subSize / 2f).toInt()

        val endCamX = cameraCoords.first
        val endCamY = cameraCoords.second
        val endCloseX = closeCoords.first
        val endCloseY = closeCoords.second

        android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 250
            interpolator = android.view.animation.OvershootInterpolator()
            addUpdateListener { anim ->
                val fraction = anim.animatedValue as Float
                subMenuParamsCamera?.x = (startX + (endCamX - startX) * fraction).toInt()
                subMenuParamsCamera?.y = (startY + (endCamY - startY) * fraction).toInt()
                subMenuParamsClose?.x = (startX + (endCloseX - startX) * fraction).toInt()
                subMenuParamsClose?.y = (startY + (endCloseY - startY) * fraction).toInt()
                
                subMenuViewCamera?.let { windowManager.updateViewLayout(it, subMenuParamsCamera) }
                subMenuViewClose?.let { windowManager.updateViewLayout(it, subMenuParamsClose) }
            }
            start()
        }

        subMenuViewCamera?.animateShow()
        subMenuViewClose?.animateShow()

        bubbleView?.postDelayed({
            if (currentState == BubbleState.OPENING_MENU) {
                currentState = BubbleState.MENU_OPEN
            }
        }, 160L)
    }

    private fun updateSubMenuPosition() {
        val (cameraCoords, closeCoords) = getSubMenuCoordinates()

        subMenuViewCamera?.let { sub ->
            subMenuParamsCamera?.let { params ->
                params.x = cameraCoords.first
                params.y = cameraCoords.second
                windowManager.updateViewLayout(sub, params)
            }
        }

        subMenuViewClose?.let { sub ->
            subMenuParamsClose?.let { params ->
                params.x = closeCoords.first
                params.y = closeCoords.second
                windowManager.updateViewLayout(sub, params)
            }
        }
    }

    private fun closeSubMenu(onClosed: (() -> Unit)? = null) {
        if (currentState != BubbleState.MENU_OPEN) {
            onClosed?.invoke()
            return
        }
        currentState = BubbleState.CLOSING_MENU

        val viewCamera = subMenuViewCamera
        val viewClose = subMenuViewClose

        if (viewCamera == null && viewClose == null) {
            currentState = BubbleState.IDLE
            onClosed?.invoke()
            return
        }

        val mainSize = resources.getDimensionPixelSize(R.dimen.bubble_main_size)
        val subSize = resources.getDimensionPixelSize(R.dimen.bubble_sub_size)
        
        val endX = (bubbleParams.x + mainSize / 2f - subSize / 2f).toInt()
        val endY = (bubbleParams.y + mainSize / 2f - subSize / 2f).toInt()

        val startCamX = subMenuParamsCamera?.x ?: 0
        val startCamY = subMenuParamsCamera?.y ?: 0
        val startCloseX = subMenuParamsClose?.x ?: 0
        val startCloseY = subMenuParamsClose?.y ?: 0

        android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 150
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
            addUpdateListener { anim ->
                val fraction = anim.animatedValue as Float
                subMenuParamsCamera?.x = (startCamX + (endX - startCamX) * fraction).toInt()
                subMenuParamsCamera?.y = (startCamY + (endY - startCamY) * fraction).toInt()
                subMenuParamsClose?.x = (startCloseX + (endX - startCloseX) * fraction).toInt()
                subMenuParamsClose?.y = (startCloseY + (endY - startCloseY) * fraction).toInt()
                
                if (subMenuViewCamera != null && subMenuParamsCamera != null) {
                    try { windowManager.updateViewLayout(subMenuViewCamera, subMenuParamsCamera) } catch (e: Exception) {}
                }
                if (subMenuViewClose != null && subMenuParamsClose != null) {
                    try { windowManager.updateViewLayout(subMenuViewClose, subMenuParamsClose) } catch (e: Exception) {}
                }
            }
            start()
        }

        viewCamera?.animateHide()
        viewClose?.animateHide()

        bubbleView?.postDelayed({
            try {
                if (viewCamera != null) windowManager.removeView(viewCamera)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                if (viewClose != null) windowManager.removeView(viewClose)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (subMenuViewCamera == viewCamera) {
                subMenuViewCamera = null
                subMenuParamsCamera = null
            }
            if (subMenuViewClose == viewClose) {
                subMenuViewClose = null
                subMenuParamsClose = null
            }

            if (currentState == BubbleState.CLOSING_MENU) {
                currentState = BubbleState.IDLE
            }
            onClosed?.invoke()
        }, 160L)
    }

    private fun startScanningProcess() {
        if (currentState == BubbleState.SCANNING || currentState == BubbleState.SHOW_RESULT) return
        
        currentState = BubbleState.SCANNING

        val viewCamera = subMenuViewCamera
        val viewClose = subMenuViewClose

        try {
            if (viewCamera != null) windowManager.removeView(viewCamera)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            if (viewClose != null) windowManager.removeView(viewClose)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        subMenuViewCamera = null
        subMenuParamsCamera = null
        subMenuViewClose = null
        subMenuParamsClose = null

        // 1. Efek kilat putih simulasi screenshot
        ScreenFlashUtil.flashScreen(this@FloatingBubbleService, 120L)

        // 2. Ubah bubble utama menjadi loading spinner berputar
        bubbleView?.setScanningState(true)

        // 3. Jalankan FakeScanEngine di background (3 detik)
        serviceScope.launch {
            val scanResult = FakeScanEngine.performFakeScan()
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
            .setContentText("Ketuk gelembung melayang untuk memindai penipuan di layar Anda.")
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        bubbleView?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
        subMenuViewCamera?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
        subMenuViewClose?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
        resultPanelView?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
    }
}
