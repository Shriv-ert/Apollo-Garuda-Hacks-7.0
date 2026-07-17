package com.garuda.floatingbubble.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.garuda.floatingbubble.R
import com.garuda.floatingbubble.data.AdminReportItem

class AdminReportAdapter(
    private var reports: List<AdminReportItem>,
    private val onVerify: (Int) -> Unit,
    private val onReject: (Int) -> Unit
) : RecyclerView.Adapter<AdminReportAdapter.AdminReportViewHolder>() {

    fun updateData(newReports: List<AdminReportItem>) {
        reports = newReports
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_report, parent, false)
        return AdminReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminReportViewHolder, position: Int) {
        val report = reports[position]

        val entitiesStr = report.entities.joinToString(", ") { it.value }
        holder.tvEntities.text = entitiesStr.ifEmpty { "(tidak ada entitas)" }
        holder.tvCategory.text = report.category
        holder.tvDesc.text = report.description

        // Show reporter name if available
        val reporterName = report.user?.fullName ?: "Anonim"
        holder.tvDesc.text = "${report.description}\n\nPelapor: $reporterName"

        holder.btnVerify.setOnClickListener {
            onVerify(report.id)
        }

        holder.btnReject.setOnClickListener {
            onReject(report.id)
        }
    }

    override fun getItemCount(): Int = reports.size

    class AdminReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEntities: TextView = itemView.findViewById(R.id.tvAdminReportEntities)
        val tvCategory: TextView = itemView.findViewById(R.id.tvAdminReportCategory)
        val tvDesc: TextView = itemView.findViewById(R.id.tvAdminReportDesc)
        val btnVerify: Button = itemView.findViewById(R.id.btnVerify)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
    }
}
