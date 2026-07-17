package com.garuda.floatingbubble.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garuda.floatingbubble.R
import com.garuda.floatingbubble.data.MockRepository

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var adapter: AdminReportAdapter
    private lateinit var tvPendingCount: TextView
    private lateinit var tvVerifiedCount: TextView
    private lateinit var rvAdminReports: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvVerifiedCount = findViewById(R.id.tvVerifiedCount)
        rvAdminReports = findViewById(R.id.rvAdminReports)

        findViewById<TextView>(R.id.btnExitAdmin).setOnClickListener {
            finish()
        }

        adapter = AdminReportAdapter(
            emptyList(),
            onVerify = { reportId ->
                MockRepository.verifyReport(reportId)
                Toast.makeText(this, "Laporan $reportId berhasil diverifikasi", Toast.LENGTH_SHORT).show()
                refreshData()
            },
            onReject = { reportId ->
                MockRepository.rejectReport(reportId)
                Toast.makeText(this, "Laporan $reportId ditolak", Toast.LENGTH_SHORT).show()
                refreshData()
            }
        )
        
        rvAdminReports.layoutManager = LinearLayoutManager(this)
        rvAdminReports.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        val allReports = MockRepository.getReports()
        val pendingReports = allReports.filter { it.status == "PENDING" }
        val verifiedReports = allReports.filter { it.status == "VERIFIED" }

        tvPendingCount.text = pendingReports.size.toString()
        tvVerifiedCount.text = verifiedReports.size.toString()

        adapter.updateData(pendingReports)
    }
}
