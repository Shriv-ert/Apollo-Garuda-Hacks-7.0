package com.garuda.floatingbubble.scanner

enum class RiskLevel {
    DANGER, WARNING, SAFE
}

data class SourceItem(
    val title: String,
    val url: String
)

data class ScanResult(
    val riskLevel: RiskLevel,
    val riskScore: Int,
    val verdictShort: String,
    val verdictDetail: String,
    val sources: List<SourceItem>,
    val target: String
)
