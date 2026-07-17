package com.garuda.floatingbubble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garuda.floatingbubble.R
import com.garuda.floatingbubble.data.AdminReportItem
import com.garuda.floatingbubble.data.ApiConfig
import com.garuda.floatingbubble.data.ReviewRequest
import com.garuda.floatingbubble.data.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminFragment : Fragment() {

    private lateinit var tvPendingCount: TextView
    private lateinit var tvVerifiedCount: TextView
    private lateinit var rvAdminReports: RecyclerView
    private lateinit var pbLoading: ProgressBar
    private lateinit var adapter: AdminReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvPendingCount = view.findViewById(R.id.tvPendingCount)
        tvVerifiedCount = view.findViewById(R.id.tvVerifiedCount)
        rvAdminReports = view.findViewById(R.id.rvAdminReports)
        pbLoading = view.findViewById<ProgressBar?>(R.id.pbAdminLoading)
            ?: ProgressBar(requireContext())

        // Hide the exit button when used as a fragment
        view.findViewById<TextView?>(R.id.btnExitAdmin)?.visibility = View.GONE

        adapter = AdminReportAdapter(
            emptyList(),
            onVerify = { reportId -> verifyReport(reportId) },
            onReject = { reportId -> rejectReport(reportId) }
        )

        rvAdminReports.layoutManager = LinearLayoutManager(requireContext())
        rvAdminReports.adapter = adapter

        loadDashboard()
        loadPendingReports()
    }

    override fun onResume() {
        super.onResume()
        loadDashboard()
        loadPendingReports()
    }

    private fun loadDashboard() {
        val token = SessionManager.bearerToken(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.getAdminDashboard(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val stats = response.body()?.data
                        tvPendingCount.text = (stats?.byStatus?.pending ?: 0).toString()
                        tvVerifiedCount.text = (stats?.byStatus?.verified ?: 0).toString()
                    }
                }
            } catch (e: Exception) {
                // Dashboard stats not critical, skip silently
            }
        }
    }

    private fun loadPendingReports() {
        val token = SessionManager.bearerToken(requireContext())
        pbLoading.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.getAdminReports(token, status = "pending")
                withContext(Dispatchers.Main) {
                    pbLoading.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        val items = response.body()?.data?.items ?: emptyList()
                        adapter.updateData(items)
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat laporan", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbLoading.visibility = View.GONE
                    Toast.makeText(requireContext(), "Tidak dapat terhubung ke server", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun verifyReport(reportId: Int) {
        val token = SessionManager.bearerToken(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.verifyReport(
                    token, 
                    reportId, 
                    ReviewRequest(reviewNote = "Laporan terverifikasi oleh admin setelah pengecekan bukti.")
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "Laporan $reportId berhasil diverifikasi", Toast.LENGTH_SHORT).show()
                        loadDashboard()
                        loadPendingReports()
                    } else {
                        Toast.makeText(requireContext(), "Gagal verifikasi: ${response.body()?.message ?: ""}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun rejectReport(reportId: Int) {
        val token = SessionManager.bearerToken(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.rejectReport(
                    token, 
                    reportId, 
                    ReviewRequest(reviewNote = "Laporan ditolak oleh admin setelah peninjauan.")
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "Laporan $reportId ditolak", Toast.LENGTH_SHORT).show()
                        loadDashboard()
                        loadPendingReports()
                    } else {
                        Toast.makeText(requireContext(), "Gagal tolak: ${response.body()?.message ?: ""}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
