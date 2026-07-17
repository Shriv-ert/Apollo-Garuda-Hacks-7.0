package com.garuda.floatingbubble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.garuda.floatingbubble.R
import com.garuda.floatingbubble.data.ApiConfig
import com.garuda.floatingbubble.data.EntityInput
import com.garuda.floatingbubble.data.SessionManager
import com.garuda.floatingbubble.data.SubmitReportRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportFragment : Fragment() {

    private lateinit var etPhone: EditText
    private lateinit var etBank: EditText
    private lateinit var etUrl: EditText
    private lateinit var etEmail: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDescription: EditText
    private lateinit var btnUploadProof: LinearLayout
    private lateinit var ivProofPreview: ImageView
    private lateinit var btnSubmitReport: Button
    private lateinit var pbLoading: ProgressBar

    // For now: proof image is a placeholder URL (real upload endpoint not yet available)
    private var proofImageUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etPhone = view.findViewById(R.id.etPhone)
        etBank = view.findViewById(R.id.etBank)
        etUrl = view.findViewById(R.id.etUrl)
        etEmail = view.findViewById(R.id.etEmail)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        etDescription = view.findViewById(R.id.etDescription)
        btnUploadProof = view.findViewById(R.id.btnUploadProof)
        ivProofPreview = view.findViewById(R.id.ivProofPreview)
        btnSubmitReport = view.findViewById(R.id.btnSubmitReport)
        pbLoading = view.findViewById(R.id.pbLoading)

        setupSpinners()
        setupListeners()
    }

    private fun setupSpinners() {
        val categories = arrayOf(
            "Pinjol Ilegal",
            "Penipuan Online",
            "Spam/Phishing",
            "Investasi Bodong",
            "Lainnya"
        )
        spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
    }

    private fun setupListeners() {
        btnUploadProof.setOnClickListener {
            // Placeholder: In production, open file picker or camera
            proofImageUrl = "https://placeholder.awam.id/bukti_${System.currentTimeMillis()}.jpg"
            ivProofPreview.visibility = View.VISIBLE
            ivProofPreview.setBackgroundColor(resources.getColor(R.color.awam_text_light, null))
            Toast.makeText(requireContext(), "Bukti diunggah (placeholder)", Toast.LENGTH_SHORT).show()
        }

        btnSubmitReport.setOnClickListener {
            submitReport()
        }
    }

    private fun submitReport() {
        val phone = etPhone.text.toString().trim()
        val bank = etBank.text.toString().trim()
        val url = etUrl.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (phone.isEmpty() && bank.isEmpty() && url.isEmpty() && email.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi minimal satu identitas penipu", Toast.LENGTH_LONG).show()
            return
        }

        if (description.length < 10) {
            etDescription.error = "Kronologi minimal 10 karakter"
            return
        }

        if (proofImageUrl == null) {
            Toast.makeText(requireContext(), "Harap unggah bukti tangkapan layar", Toast.LENGTH_SHORT).show()
            return
        }

        val category = spinnerCategory.selectedItem.toString()

        // Build entities list matching backend DTO shape
        val entities = mutableListOf<EntityInput>()
        if (phone.isNotEmpty()) entities.add(EntityInput(entityValue = phone, entityType = "phone"))
        if (bank.isNotEmpty()) entities.add(EntityInput(entityValue = bank, entityType = "bank_account"))
        if (url.isNotEmpty()) entities.add(EntityInput(entityValue = url, entityType = "url"))
        if (email.isNotEmpty()) entities.add(EntityInput(entityValue = email, entityType = "email"))

        val token = SessionManager.bearerToken(requireContext())

        btnSubmitReport.isEnabled = false
        pbLoading.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.submitReport(
                    token = token,
                    request = SubmitReportRequest(
                        entities = entities,
                        category = category,
                        description = description,
                        proofImage = proofImageUrl!!
                    )
                )
                withContext(Dispatchers.Main) {
                    pbLoading.visibility = View.GONE
                    btnSubmitReport.isEnabled = true
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            requireContext(),
                            "Berhasil mengirim ${entities.size} laporan! Menunggu verifikasi.",
                            Toast.LENGTH_LONG
                        ).show()
                        resetForm()
                    } else {
                        val msg = response.body()?.message ?: "Gagal mengirim laporan"
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbLoading.visibility = View.GONE
                    btnSubmitReport.isEnabled = true
                    Toast.makeText(requireContext(), "Gagal terhubung: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun resetForm() {
        etPhone.text.clear()
        etBank.text.clear()
        etUrl.text.clear()
        etEmail.text.clear()
        etDescription.text.clear()
        ivProofPreview.visibility = View.GONE
        proofImageUrl = null
        spinnerCategory.setSelection(0)
    }
}
