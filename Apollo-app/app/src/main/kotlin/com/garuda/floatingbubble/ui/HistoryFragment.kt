package com.garuda.floatingbubble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.garuda.floatingbubble.R
import com.garuda.floatingbubble.scanner.HistoryManager

class HistoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = view.findViewById<LinearLayout>(R.id.historyContainer)
        val historyList = HistoryManager.getHistory()

        container.removeAllViews()

        if (historyList.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text = "Belum ada riwayat"
                setTextColor(resources.getColor(R.color.awam_text_light, null))
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 64, 0, 0)
            }
            container.addView(empty)
        } else {
            for (item in historyList) {
                val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_history, container, false)
                itemView.findViewById<TextView>(R.id.tvHistoryTarget).text = item.target
                itemView.findViewById<TextView>(R.id.tvHistoryVerdict).text = "${item.verdictShort} (${item.riskScore}%)"
                val tvText = itemView.findViewById<TextView>(R.id.tvHistoryText)
                if (!item.scannedText.isNullOrEmpty()) {
                    tvText.text = item.scannedText
                    tvText.visibility = View.VISIBLE
                } else {
                    tvText.visibility = View.GONE
                }
                container.addView(itemView)
            }
        }
    }
}
