package com.garuda.floatingbubble.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.garuda.floatingbubble.MainActivity
import com.garuda.floatingbubble.R

class PermissionActivity : AppCompatActivity() {

    private val OVERLAY_REQ = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        findViewById<Button>(R.id.btnAllow).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                    OVERLAY_REQ
                )
            }
        }

        findViewById<Button>(R.id.btnSkip).setOnClickListener {
            markAsked()
            goToDashboard()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_REQ) {
            markAsked()
            goToDashboard()
        }
    }

    private fun markAsked() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("overlay_asked", true)
            .apply()
    }

    private fun goToDashboard() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
