package com.garuda.floatingbubble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.garuda.floatingbubble.R
import com.garuda.floatingbubble.data.ApiConfig
import com.garuda.floatingbubble.data.ReportItem
import com.garuda.floatingbubble.data.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadHistory()
    }

    private fun loadHistory() {
        val view = view ?: return
        val container = view.findViewById<LinearLayout>(R.id.historyContainer)
        val pbLoading = view.findViewById<ProgressBar?>(R.id.pbHistoryLoading)

        pbLoading?.visibility = View.VISIBLE
        container.removeAllViews()

        val token = SessionManager.bearerToken(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.getReportHistory(token)
                withContext(Dispatchers.Main) {
                    pbLoading?.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        val items = response.body()?.data?.items ?: emptyList()
                        renderHistory(container, items)
                    } else {
                        showEmpty(container, "Gagal memuat riwayat")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbLoading?.visibility = View.GONE
                    showEmpty(container, "Tidak dapat terhubung ke server")
                }
            }
        }
    }

    private fun renderHistory(container: LinearLayout, historyList: List<ReportItem>) {
        if (historyList.isEmpty()) {
            showEmpty(container, "Belum ada riwayat laporan")
            return
        }

        for (report in historyList) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_history, container, false)

            // Show entities values joined
            val entitiesStr = report.entities.joinToString(", ") { it.value }
            itemView.findViewById<TextView>(R.id.tvHistoryTarget).text =
                entitiesStr.ifEmpty { "(tidak ada entitas)" }

            // Show category + status
            val tvVerdict = itemView.findViewById<TextView>(R.id.tvHistoryVerdict)
            tvVerdict.text = "${report.category} · ${report.status.uppercase()}"
            val verdictColor = when (report.status) {
                "verified" -> resources.getColor(R.color.awam_safe, null)
                "rejected" -> resources.getColor(R.color.awam_danger, null)
                "reviewing" -> resources.getColor(R.color.awam_warning, null)
                else -> resources.getColor(R.color.awam_text_light, null)
            }
            tvVerdict.setTextColor(verdictColor)

            // Description
            val tvText = itemView.findViewById<TextView>(R.id.tvHistoryText)
            if (report.description.isNotEmpty()) {
                tvText.text = report.description
                tvText.visibility = View.VISIBLE
            } else {
                tvText.visibility = View.GONE
            }

            container.addView(itemView)
        }
    }

    private fun showEmpty(container: LinearLayout, message: String) {
        val empty = TextView(requireContext()).apply {
            text = message
            setTextColor(resources.getColor(R.color.awam_text_light, null))
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 64, 0, 0)
        }
        container.addView(empty)
    }
}
