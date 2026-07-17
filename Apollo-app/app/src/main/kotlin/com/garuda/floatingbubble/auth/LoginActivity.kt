package com.garuda.floatingbubble.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.garuda.floatingbubble.MainActivity
import com.garuda.floatingbubble.R
import com.garuda.floatingbubble.data.ApiConfig
import com.garuda.floatingbubble.data.LoginRequest
import com.garuda.floatingbubble.data.SessionManager
import com.garuda.floatingbubble.ui.AdminDashboardActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvToRegister = findViewById<TextView>(R.id.tvToRegister)
        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            pbLoading.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiConfig.apiService.login(LoginRequest(email, password))
                    withContext(Dispatchers.Main) {
                        pbLoading.visibility = View.GONE
                        if (response.isSuccessful && response.body()?.success == true) {
                            val authData = response.body()!!.data
                            SessionManager.saveSession(
                                context = this@LoginActivity,
                                token = authData.token,
                                userId = authData.user.id,
                                email = authData.user.email,
                                fullName = authData.user.fullName,
                                role = authData.user.role
                            )
                            Toast.makeText(this@LoginActivity, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                            val intent = if (authData.user.role == "admin") {
                                Intent(this@LoginActivity, AdminDashboardActivity::class.java)
                            } else {
                                Intent(this@LoginActivity, MainActivity::class.java)
                            }
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            val msg = response.body()?.message ?: "Email atau password salah"
                            Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                            btnLogin.isEnabled = true
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        pbLoading.visibility = View.GONE
                        btnLogin.isEnabled = true
                        Toast.makeText(this@LoginActivity, "Gagal terhubung ke server: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
