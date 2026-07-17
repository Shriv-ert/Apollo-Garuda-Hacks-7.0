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
import com.garuda.floatingbubble.data.RegisterRequest
import com.garuda.floatingbubble.data.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

            btnRegister.isEnabled = false
            pbLoading.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Backend expects full_name (mapped by @SerializedName in RegisterRequest)
                    val response = ApiConfig.apiService.register(
                        RegisterRequest(email = email, fullName = name, password = password)
                    )
                    withContext(Dispatchers.Main) {
                        pbLoading.visibility = View.GONE
                        if (response.isSuccessful && response.body()?.success == true) {
                            val authData = response.body()!!.data
                            SessionManager.saveSession(
                                context = this@RegisterActivity,
                                token = authData.token,
                                userId = authData.user.id,
                                email = authData.user.email,
                                fullName = authData.user.fullName,
                                role = authData.user.role
                            )
                            Toast.makeText(this@RegisterActivity, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@RegisterActivity, MainActivity::class.java)
                                .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
                            finish()
                        } else {
                            val msg = response.body()?.message ?: "Registrasi gagal"
                            Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_SHORT).show()
                            btnRegister.isEnabled = true
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        pbLoading.visibility = View.GONE
                        btnRegister.isEnabled = true
                        Toast.makeText(this@RegisterActivity, "Gagal terhubung ke server: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        tvToLogin.setOnClickListener {
            finish()
        }
    }
}
