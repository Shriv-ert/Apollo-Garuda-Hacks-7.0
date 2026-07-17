package com.garuda.floatingbubble.scanner

import kotlinx.coroutines.delay

object FakeScanEngine {
    suspend fun performFakeScan(text: String? = null): ScanResult {
        delay(3000)
        val result = ScanResult(
            riskLevel = RiskLevel.DANGER,
            riskScore = 87,
            verdictShort = "BAHAYA",
            verdictDetail = "Domain jnt-express-id.xyz baru didaftarkan 4 hari lalu dan bukan domain resmi J&T Express (jet.co.id). Nomor 08123456789 ditemukan dalam 2 laporan penipuan online.",
            sources = listOf(
                SourceItem("WHOIS - jnt-express-id.xyz", "https://whois.domaintools.com/jnt-express-id.xyz"),
                SourceItem("jet.co.id - Situs Resmi", "https://jet.co.id"),
                SourceItem("Laporan Forum Online Kredibel", "https://cekrekening.id")
            ),
            target = "jnt-express-id.xyz",
            scannedText = text
        )
        HistoryManager.addHistory(result)
        return result
    }
    suspend fun performInstantFakeScan(text: String? = null): ScanResult {
        val result = ScanResult(
            riskLevel = RiskLevel.DANGER,
            riskScore = 87,
            verdictShort = "BAHAYA",
            verdictDetail = "Domain jnt-express-id.xyz baru didaftarkan 4 hari lalu dan bukan domain resmi J&T Express (jet.co.id). Nomor 08123456789 ditemukan dalam 2 laporan penipuan online.",
            sources = listOf(
                SourceItem("WHOIS - jnt-express-id.xyz", "https://whois.domaintools.com/jnt-express-id.xyz"),
                SourceItem("jet.co.id - Situs Resmi", "https://jet.co.id"),
                SourceItem("Laporan Forum Online Kredibel", "https://cekrekening.id")
            ),
            target = "jnt-express-id.xyz",
            scannedText = text
        )
        HistoryManager.addHistory(result)
        return result
    }
}
