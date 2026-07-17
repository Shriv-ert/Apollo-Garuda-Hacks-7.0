package com.garuda.floatingbubble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.garuda.floatingbubble.R
import com.garuda.floatingbubble.scanner.HistoryManager
import com.garuda.floatingbubble.scanner.RiskLevel

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val historyContainer = findViewById<LinearLayout>(R.id.historyContainer)
        val scanHistory = HistoryManager.getHistory()

        if (scanHistory.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "Belum ada riwayat pemindaian"
                setTextColor(resources.getColor(R.color.awam_surface, null))
                textSize = 16f
                gravity = android.view.Gravity.CENTER
            }
            historyContainer.addView(emptyText)
        } else {
            for (result in scanHistory) {
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_history, historyContainer, false)

                val tvHistoryTarget = itemView.findViewById<TextView>(R.id.tvHistoryTarget)
                val tvHistoryVerdict = itemView.findViewById<TextView>(R.id.tvHistoryVerdict)
                val tvHistoryText = itemView.findViewById<TextView>(R.id.tvHistoryText)

                tvHistoryTarget.text = result.target
                tvHistoryVerdict.text = result.verdictShort

                val verdictColor = when (result.riskLevel) {
                    RiskLevel.DANGER -> resources.getColor(R.color.awam_danger, null)
                    RiskLevel.WARNING -> resources.getColor(R.color.awam_warning, null)
                    RiskLevel.SAFE -> resources.getColor(R.color.awam_safe, null)
                }
                tvHistoryVerdict.setTextColor(verdictColor)

                if (!result.scannedText.isNullOrEmpty()) {
                    tvHistoryText.text = result.scannedText
                    tvHistoryText.visibility = View.VISIBLE
                } else {
                    tvHistoryText.visibility = View.GONE
                }

                historyContainer.addView(itemView)
            }
        }
    }
}
