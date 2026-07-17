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
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.garuda.floatingbubble.R

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

    private var mockProofUrl: String? = null

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
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        spinnerCategory.adapter = categoryAdapter
    }

    private fun setupListeners() {
        btnUploadProof.setOnClickListener {
            // Mocking proof upload
            mockProofUrl = "https://mock-image-server.com/bukti.jpg"
            ivProofPreview.visibility = View.VISIBLE
            // Use a solid color or a placeholder icon to simulate image loading
            ivProofPreview.setBackgroundColor(resources.getColor(R.color.awam_text_light, null))
            Toast.makeText(requireContext(), "Bukti berhasil diunggah (Mock)", Toast.LENGTH_SHORT).show()
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

        if (mockProofUrl == null) {
            Toast.makeText(requireContext(), "Harap unggah bukti tangkapan layar", Toast.LENGTH_SHORT).show()
            return
        }

        val category = spinnerCategory.selectedItem.toString()

        val filledEntities = mutableListOf<Pair<String, String>>()
        if (phone.isNotEmpty()) filledEntities.add(Pair("phone", phone))
        if (bank.isNotEmpty()) filledEntities.add(Pair("bank_account", bank))
        if (url.isNotEmpty()) filledEntities.add(Pair("url", url))
        if (email.isNotEmpty()) filledEntities.add(Pair("email", email))

        // Simpan ke MockRepository secara lokal
        com.garuda.floatingbubble.data.MockRepository.addReport(filledEntities, category, description)

        Toast.makeText(requireContext(), "Berhasil mengirim ${filledEntities.size} laporan secara bersamaan!", Toast.LENGTH_LONG).show()
        
        // Reset form
        etPhone.text.clear()
        etBank.text.clear()
        etUrl.text.clear()
        etEmail.text.clear()
        etDescription.text.clear()
        ivProofPreview.visibility = View.GONE
        mockProofUrl = null
        spinnerCategory.setSelection(0)
    }
}
