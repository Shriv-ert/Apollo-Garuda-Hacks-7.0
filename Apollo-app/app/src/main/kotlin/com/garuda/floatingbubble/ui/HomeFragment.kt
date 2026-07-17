package com.garuda.floatingbubble.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.garuda.floatingbubble.R

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(requireContext())
        } else true
        
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isBubbleEnabled = prefs.getBoolean("bubble_enabled", true)

        if (!hasOverlay) {
            tvStatus.text = "Izin overlay belum diberikan"
        } else if (!isBubbleEnabled) {
            tvStatus.text = "Awam dinonaktifkan dari Pengaturan"
        } else {
            tvStatus.text = "Awam siap (Otomatis muncul di luar aplikasi)"
        }
    }
}
