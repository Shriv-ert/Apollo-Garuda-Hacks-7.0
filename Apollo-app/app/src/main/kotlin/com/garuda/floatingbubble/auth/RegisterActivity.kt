package com.garuda.floatingbubble.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.garuda.floatingbubble.MainActivity
import com.garuda.floatingbubble.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val tvToLogin = findViewById<TextView>(R.id.tvToLogin)
        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Nama lengkap wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mockup proses register (Happy Path 1A / Error 1B)
            btnRegister.isEnabled = false
            pbLoading.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.Main).launch {
                delay(1500) // Simulasi loading API

                if (email == "already@exist.com") {
                    // Simulasi email sudah ada
                    Toast.makeText(this@RegisterActivity, "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
                    btnRegister.isEnabled = true
                    pbLoading.visibility = View.GONE
                } else {
                    // Registrasi Berhasil
                    val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    sharedPref.edit().putBoolean("is_logged_in", true).apply()

                    Toast.makeText(this@RegisterActivity, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    // Clear backstack agar user tidak bisa kembali ke Register/Login setelah masuk
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }

        tvToLogin.setOnClickListener {
            finish() // Kembali ke LoginActivity
        }
    }
}
