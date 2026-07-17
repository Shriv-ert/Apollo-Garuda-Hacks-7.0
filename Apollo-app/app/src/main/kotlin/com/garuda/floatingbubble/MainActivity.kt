package com.garuda.floatingbubble

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.garuda.floatingbubble.data.SessionManager
import com.garuda.floatingbubble.ui.AdminFragment
import com.garuda.floatingbubble.ui.DatabaseFragment
import com.garuda.floatingbubble.ui.HistoryFragment
import com.garuda.floatingbubble.ui.HomeFragment
import com.garuda.floatingbubble.ui.PermissionActivity
import com.garuda.floatingbubble.ui.ReportFragment
import com.garuda.floatingbubble.ui.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, com.garuda.floatingbubble.auth.LoginActivity::class.java))
            finish()
            return
        }

        if (SessionManager.isAdmin(this)) {
            startActivity(Intent(this, com.garuda.floatingbubble.ui.AdminDashboardActivity::class.java))
            finish()
            return
        }

        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (!hasOverlay && !prefs.getBoolean("overlay_asked", false)) {
            startActivity(Intent(this, PermissionActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        if (hasOverlay) {
            val isBubbleEnabled = prefs.getBoolean("bubble_enabled", true)
            if (isBubbleEnabled) {
                startFloatingBubbleService()
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val fabReport = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabReport)

        // Show Admin tab only for admin role
        val isAdmin = SessionManager.isAdmin(this)
        bottomNav.menu.findItem(R.id.nav_admin)?.isVisible = isAdmin

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            bottomNav.selectedItemId = R.id.nav_home
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { loadFragment(HomeFragment()); true }
                R.id.nav_history -> { loadFragment(HistoryFragment()); true }
                R.id.nav_database -> { loadFragment(DatabaseFragment()); true }
                R.id.nav_admin -> { loadFragment(AdminFragment()); true }
                R.id.nav_settings -> { loadFragment(SettingsFragment()); true }
                else -> false
            }
        }

        fabReport.setOnClickListener {
            bottomNav.menu.setGroupCheckable(0, false, true)
            loadFragment(ReportFragment())
            // Re-enable checkable so user can re-select other tabs
            bottomNav.menu.setGroupCheckable(0, true, true)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun startFloatingBubbleService() {
        val intent = Intent(this, FloatingBubbleService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
