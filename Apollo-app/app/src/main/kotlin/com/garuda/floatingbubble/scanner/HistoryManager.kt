package com.garuda.floatingbubble.scanner

object HistoryManager {
    private val historyList = mutableListOf<ScanResult>()

    fun addHistory(result: ScanResult) {
        historyList.add(0, result)
    }

    fun getHistory(): List<ScanResult> {
        return historyList.toList()
    }
}
