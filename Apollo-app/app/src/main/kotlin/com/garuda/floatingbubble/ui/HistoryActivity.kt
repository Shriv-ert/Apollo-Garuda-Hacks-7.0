package com.garuda.floatingbubble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.garuda.floatingbubble.R
import com.garuda.floatingbubble.scanner.HistoryManager

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val historyContainer = findViewById<LinearLayout>(R.id.historyContainer)
        val historyList = HistoryManager.getHistory()

        if (historyList.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "Belum ada riwayat"
                setTextColor(resources.getColor(R.color.awam_surface, null))
                textSize = 16f
                gravity = android.view.Gravity.CENTER
            }
            historyContainer.addView(emptyText)
        } else {
            for (item in historyList) {
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_history, historyContainer, false)
                
                val tvHistoryTarget = itemView.findViewById<TextView>(R.id.tvHistoryTarget)
                val tvHistoryVerdict = itemView.findViewById<TextView>(R.id.tvHistoryVerdict)
                val tvHistoryText = itemView.findViewById<TextView>(R.id.tvHistoryText)

                tvHistoryTarget.text = item.target
                tvHistoryVerdict.text = "${item.verdictShort} (${item.riskScore}%)"
                if (!item.scannedText.isNullOrEmpty()) {
                    tvHistoryText.text = "Teks: ${item.scannedText}"
                    tvHistoryText.visibility = View.VISIBLE
                } else {
                    tvHistoryText.visibility = View.GONE
                }

                historyContainer.addView(itemView)
            }
        }
    }
}
