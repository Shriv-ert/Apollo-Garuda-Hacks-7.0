package com.garuda.floatingbubble.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.garuda.floatingbubble.R
import com.garuda.floatingbubble.scanner.RiskLevel
import com.garuda.floatingbubble.scanner.ScanResult

class ResultPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val panelContainer: View
    private val headerBar: RelativeLayout
    private val btnClose: ImageButton
    private val riskBarLayout: FrameLayout
    private val tvRiskLevel: TextView
    private val tvRiskScore: TextView
    private val tvExplanation: TextView
    private val sourcesListContainer: LinearLayout
    private val btnShare: Button
    private val tvTargetValue: TextView
    private val tvScamBadge: TextView
    
    private val btnToggleDetails: TextView
    private val detailedContainer: LinearLayout

    var onCloseClicked: (() -> Unit)? = null
    var onPanelDragged: ((Int, Int) -> Unit)? = null
    var onPanelTouchDown: (() -> Unit)? = null
    var onPanelSizeChanged: (() -> Unit)? = null

    private var currentResult: ScanResult? = null
    private var initialX = 0f
    private var initialY = 0f
    private var isExpanded = false

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_result_panel, this, true)

        panelContainer = findViewById(R.id.panelContainer)
        headerBar = findViewById(R.id.headerBar)
        btnClose = findViewById(R.id.btnClose)
        riskBarLayout = findViewById(R.id.riskBarLayout)
        tvRiskLevel = findViewById(R.id.tvRiskLevel)
        tvRiskScore = findViewById(R.id.tvRiskScore)
        tvExplanation = findViewById(R.id.tvExplanation)
        sourcesListContainer = findViewById(R.id.sourcesListContainer)
        btnShare = findViewById(R.id.btnShare)
        tvTargetValue = findViewById(R.id.tvTargetValue)
        tvScamBadge = findViewById(R.id.tvScamBadge)
        
        btnToggleDetails = findViewById(R.id.btnToggleDetails)
        detailedContainer = findViewById(R.id.detailedContainer)

        btnClose.setOnClickListener { onCloseClicked?.invoke() }

        // Fitur Drag di Header
        headerBar.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX
                    initialY = event.rawY
                    onPanelTouchDown?.invoke()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialX).toInt()
                    val deltaY = (event.rawY - initialY).toInt()
                    onPanelDragged?.invoke(deltaX, deltaY)
                    true
                }
                else -> false
            }
        }

        // Toggle Details
        btnToggleDetails.setOnClickListener {
            val transition = android.transition.AutoTransition().apply { duration = 200 }
            android.transition.TransitionManager.beginDelayedTransition(this, transition)
            
            isExpanded = !isExpanded
            if (isExpanded) {
                detailedContainer.visibility = View.VISIBLE
                btnToggleDetails.text = "Lebih Ringkas ▲"
            } else {
                detailedContainer.visibility = View.GONE
                btnToggleDetails.text = "Lihat Detail ▼"
            }
            
            // Note: onPanelSizeChanged is not invoked manually here 
            // so that WindowManager doesn't snap the bounds during the AutoTransition.
        }

        btnShare.setOnClickListener {
            currentResult?.let { result ->
                val shareText = "AWAM Scan Report:\nVerdict: ${result.verdictShort} (${result.riskScore}%)\nDetail: ${result.verdictDetail}"
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("AWAM Report", shareText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, R.string.share_toast_message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun bindScanResult(result: ScanResult) {
        this.currentResult = result
        tvRiskLevel.text = result.verdictShort
        tvRiskScore.text = "${result.riskScore}%"
        tvExplanation.text = result.verdictDetail

        when (result.riskLevel) {
            RiskLevel.DANGER -> riskBarLayout.setBackgroundResource(R.drawable.bg_risk_bar_red)
            RiskLevel.WARNING -> riskBarLayout.setBackgroundResource(R.drawable.bg_risk_bar_yellow)
            RiskLevel.SAFE -> riskBarLayout.setBackgroundResource(R.drawable.bg_risk_bar_green)
        }

        tvTargetValue.text = result.target
        when (result.riskLevel) {
            RiskLevel.DANGER -> {
                tvScamBadge.visibility = View.VISIBLE
                tvScamBadge.text = "SCAM"
                tvScamBadge.setBackgroundResource(R.drawable.bg_risk_bar_red)
            }
            RiskLevel.WARNING -> {
                tvScamBadge.visibility = View.VISIBLE
                tvScamBadge.text = "SCAM"
                tvScamBadge.setBackgroundResource(R.drawable.bg_risk_bar_yellow)
            }
            RiskLevel.SAFE -> {
                tvScamBadge.visibility = View.VISIBLE
                tvScamBadge.text = "AMAN"
                tvScamBadge.setBackgroundResource(R.drawable.bg_risk_bar_green)
            }
        }

        sourcesListContainer.removeAllViews()
        for (source in result.sources) {
            val sourceRow = LayoutInflater.from(context).inflate(R.layout.item_source_row, sourcesListContainer, false)
            val tvSourceTitle = sourceRow.findViewById<TextView>(R.id.tvSourceTitle)
            tvSourceTitle.text = source.title
            sourcesListContainer.addView(sourceRow)
        }
    }

    fun animateSlideUp() {
        visibility = View.VISIBLE
        val anim = AnimationUtils.loadAnimation(context, R.anim.panel_slide_up)
        panelContainer.startAnimation(anim)
    }

    fun animateSlideDown(onComplete: (() -> Unit)? = null) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.panel_slide_down)
        anim.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                visibility = View.GONE
                onComplete?.invoke()
            }
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })
        panelContainer.startAnimation(anim)
    }
}
