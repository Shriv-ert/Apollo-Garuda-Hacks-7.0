package com.garuda.floatingbubble

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.garuda.floatingbubble.ui.HistoryFragment
import com.garuda.floatingbubble.ui.HomeFragment
import com.garuda.floatingbubble.ui.PermissionActivity
import com.garuda.floatingbubble.ui.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        if (!prefs.getBoolean("is_logged_in", false)) {
            startActivity(Intent(this, com.garuda.floatingbubble.auth.LoginActivity::class.java))
            finish()
            return
        }

        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true

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

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            bottomNav.selectedItemId = R.id.nav_home
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_history -> loadFragment(HistoryFragment())
                R.id.nav_database -> loadFragment(com.garuda.floatingbubble.ui.DatabaseFragment())
                R.id.nav_settings -> loadFragment(SettingsFragment())
                R.id.nav_placeholder -> return@setOnItemSelectedListener false // Do nothing if placeholder is clicked
            }
            true
        }

        fabReport.setOnClickListener {
            // Uncheck other items or select placeholder
            bottomNav.menu.findItem(R.id.nav_placeholder).isChecked = true
            loadFragment(com.garuda.floatingbubble.ui.ReportFragment())
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
