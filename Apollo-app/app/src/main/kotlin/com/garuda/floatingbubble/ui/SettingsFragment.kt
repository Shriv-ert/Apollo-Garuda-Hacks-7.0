package com.garuda.floatingbubble.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.garuda.floatingbubble.FloatingBubbleService
import com.garuda.floatingbubble.R
import com.garuda.floatingbubble.auth.LoginActivity
import com.garuda.floatingbubble.data.SessionManager

class SettingsFragment : Fragment() {

    private lateinit var tvOverlay: TextView
    private lateinit var tvBubble: TextView
    private lateinit var rowOverlay: LinearLayout
    private lateinit var rowBubble: LinearLayout
    private val OVERLAY_REQ = 5678

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvOverlay = view.findViewById(R.id.tvOverlayStatus)
        tvBubble = view.findViewById(R.id.tvBubbleStatus)
        rowOverlay = view.findViewById(R.id.rowOverlay)
        rowBubble = view.findViewById(R.id.rowBubble)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnAdminDashboard = view.findViewById<Button>(R.id.btnAdminDashboard)

        // Populate user info from SessionManager
        view.findViewById<TextView>(R.id.tvUserName).text =
            SessionManager.getFullName(requireContext()).ifEmpty { "Pengguna Awam" }
        view.findViewById<TextView>(R.id.tvUserEmail).text =
            SessionManager.getEmail(requireContext()).ifEmpty { "—" }

        // Admin button: only visible for admin role
        btnAdminDashboard.visibility = if (SessionManager.isAdmin(requireContext())) View.VISIBLE else View.GONE
        btnAdminDashboard.setOnClickListener {
            startActivity(Intent(requireContext(), AdminDashboardActivity::class.java))
        }

        rowOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:${requireContext().packageName}")),
                    OVERLAY_REQ
                )
            }
        }

        rowBubble.setOnClickListener {
            val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(requireContext())
            } else true

            if (!hasOverlay) {
                rowOverlay.setBackgroundColor(resources.getColor(R.color.awam_warning, null))
                rowOverlay.postDelayed({
                    rowOverlay.setBackgroundResource(R.color.awam_card_bg)
                }, 500)
                Toast.makeText(requireContext(), "Berikan izin overlay terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            val currentState = prefs.getBoolean("bubble_enabled", true)
            prefs.edit().putBoolean("bubble_enabled", !currentState).apply()

            if (currentState) {
                requireContext().stopService(Intent(requireContext(), FloatingBubbleService::class.java))
            }
            refreshUI()
        }

        btnLogout.setOnClickListener {
            SessionManager.clearSession(requireContext())
            requireContext().stopService(Intent(requireContext(), FloatingBubbleService::class.java))
            Toast.makeText(requireContext(), "Logout Berhasil", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java)
                .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
            requireActivity().finish()
        }

        refreshUI()
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun refreshUI() {
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(requireContext())
        } else true

        if (hasOverlay) {
            tvOverlay.text = "Aktif"
            tvOverlay.setTextColor(resources.getColor(R.color.awam_safe, null))
        } else {
            tvOverlay.text = "Tidak aktif (Sentuh untuk mengatur)"
            tvOverlay.setTextColor(resources.getColor(R.color.awam_warning, null))
        }

        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val isBubbleEnabled = prefs.getBoolean("bubble_enabled", true)

        if (!hasOverlay) {
            tvBubble.text = "Tidak tersedia"
            tvBubble.setTextColor(resources.getColor(R.color.awam_text_light, null))
        } else if (isBubbleEnabled) {
            tvBubble.text = "Aktif"
            tvBubble.setTextColor(resources.getColor(R.color.awam_safe, null))
        } else {
            tvBubble.text = "Nonaktif"
            tvBubble.setTextColor(resources.getColor(R.color.awam_text_light, null))
        }
    }
}
