package com.garuda.floatingbubble

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val OVERLAY_PERMISSION_REQ_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Pengecekan Login
        val sharedPref = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)
        
        if (!isLoggedIn) {
            val intent = Intent(this, com.garuda.floatingbubble.auth.LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val btnStartBubble = findViewById<Button>(R.id.btnStartBubble)
        btnStartBubble.setOnClickListener {
            if (checkOverlayPermission()) {
                startFloatingBubbleService()
            } else {
                requestOverlayPermission()
            }
        }

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            sharedPref.edit().putBoolean("is_logged_in", false).apply()
            
            // Hentikan service jika sedang berjalan
            val serviceIntent = Intent(this, FloatingBubbleService::class.java)
            stopService(serviceIntent)
            
            Toast.makeText(this, "Logout Berhasil", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, com.garuda.floatingbubble.auth.LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (checkOverlayPermission()) {
                startFloatingBubbleService()
            } else {
                Toast.makeText(this, "Izin overlay ditolak. AWAM tidak dapat menampilkan gelembung.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startFloatingBubbleService() {
        val intent = Intent(this, FloatingBubbleService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Mengaktifkan AWAM Floating Bubble...", Toast.LENGTH_SHORT).show()
    }
}
