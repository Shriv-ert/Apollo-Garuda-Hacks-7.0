package com.garuda.floatingbubble.scanner

import android.content.Context
import com.garuda.floatingbubble.data.ApiConfig
import com.garuda.floatingbubble.data.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull

/**
 * ScanEngine: Real implementation calling the backend API for text/paste scans.
 * Camera scan (screenshot via MediaProjection) requires additional permissions
 * and is deferred — this engine handles paste/clipboard text scanning.
 */
object ScanEngine {

    suspend fun scanText(context: Context, text: String): ScanResult {
        val token = SessionManager.bearerToken(context)

        try {
            val response = ApiConfig.apiService.checkText(
                token = token,
                request = com.garuda.floatingbubble.data.CheckRequest(value = text)
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val checkResult = response.body()!!.data!!

                val riskLevel = when {
                    checkResult.verdict.contains("BAHAYA", ignoreCase = true) -> RiskLevel.DANGER
                    checkResult.verdict.contains("AMAN", ignoreCase = true) -> RiskLevel.SAFE
                    else -> RiskLevel.WARNING
                }

                val result = ScanResult(
                    riskLevel = riskLevel,
                    riskScore = checkResult.riskScore,
                    verdictShort = checkResult.verdict,
                    verdictDetail = checkResult.reason,
                    sources = emptyList(),
                    target = formatTargetLabel(checkResult.entityType, checkResult.entityValue),
                    scannedText = text
                )

                HistoryManager.addHistory(result)
                return result
            } else {
                // API error – return WASPADA with message
                return buildFallbackResult(text, "Tidak dapat memverifikasi. ${response.body()?.message ?: ""}")
            }
        } catch (e: Exception) {
            return buildFallbackResult(text, "Tidak dapat terhubung ke server. Coba lagi nanti.")
        }
    }

    suspend fun scanImage(context: Context, imageBytes: ByteArray): ScanResult {
        val token = SessionManager.bearerToken(context)

        try {
            val requestFile = okhttp3.RequestBody.create(
                "image/jpeg".toMediaTypeOrNull(),
                imageBytes
            )
            val body = okhttp3.MultipartBody.Part.createFormData("image", "screenshot.jpg", requestFile)

            val response = ApiConfig.apiService.checkImage(
                token = token,
                image = body
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val checkImageResp = response.body()!!.data!!
                val checkResult = checkImageResp.summary

                val riskLevel = when {
                    checkResult.verdict.contains("BAHAYA", ignoreCase = true) -> RiskLevel.DANGER
                    checkResult.verdict.contains("AMAN", ignoreCase = true) -> RiskLevel.SAFE
                    else -> RiskLevel.WARNING
                }

                val result = ScanResult(
                    riskLevel = riskLevel,
                    riskScore = checkResult.riskScore,
                    verdictShort = checkResult.verdict,
                    verdictDetail = checkResult.reason,
                    sources = emptyList(),
                    target = formatTargetLabel(checkResult.entityType, checkResult.entityValue),
                    scannedText = "Gambar Screenshot (${formatTargetLabel(checkResult.entityType, checkResult.entityValue)})"
                )

                HistoryManager.addHistory(result)
                return result
            } else {
                return buildFallbackResult("Gambar Screenshot", "Gagal memproses gambar. ${response.body()?.message ?: ""}")
            }
        } catch (e: Exception) {
            return buildFallbackResult("Gambar Screenshot", "Tidak dapat terhubung ke server untuk memindai gambar.")
        }
    }

    private fun formatTargetLabel(entityType: String?, entityValue: String?): String {
        val valClean = entityValue?.trim() ?: ""
        if (valClean.isEmpty() || valClean.equals("Screenshot", ignoreCase = true)) {
            return "Entitas Tidak Dikenal"
        }
        val label = when (entityType?.lowercase()) {
            "phone" -> "No HP"
            "bank_account" -> "Rekening Bank"
            "url" -> "Link"
            "email" -> "Email"
            else -> null
        }
        return if (label != null) "$label: $valClean" else valClean
    }

    private fun buildFallbackResult(text: String, reason: String): ScanResult {
        val result = ScanResult(
            riskLevel = RiskLevel.WARNING,
            riskScore = 45,
            verdictShort = "WASPADA",
            verdictDetail = reason,
            sources = emptyList(),
            target = text.take(40),
            scannedText = text
        )
        HistoryManager.addHistory(result)
        return result
    }
}
