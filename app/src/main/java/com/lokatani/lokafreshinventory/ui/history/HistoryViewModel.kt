package com.lokatani.lokafreshinventory.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lokatani.lokafreshinventory.data.local.ScanResultRepository
import com.lokatani.lokafreshinventory.data.local.entity.ScanResult
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val scanResultRepository: ScanResultRepository
) : ViewModel() {
    fun getAllResult() = scanResultRepository.getAllResult()

    fun deleteScanResult(scanResult: ScanResult) {
        viewModelScope.launch {
            scanResultRepository.deleteScanResult(scanResult)
        }
    }
}