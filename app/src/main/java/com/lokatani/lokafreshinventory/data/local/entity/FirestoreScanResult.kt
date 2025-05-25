package com.lokatani.lokafreshinventory.data.local.entity

data class FirestoreScanResult(
    val user: String = "",
    val vegResult: String = "",
    val vegWeight: Int = 0,
    val date: String = ""
) {
    companion object {
        fun fromScanResult(scanResult: ScanResult): FirestoreScanResult {
            return FirestoreScanResult(
                user = scanResult.user,
                vegResult = scanResult.vegResult,
                vegWeight = scanResult.vegWeight,
                date = scanResult.date
            )
        }
    }
}
