package com.lokatani.lokafreshinventory.ui.history

import androidx.lifecycle.ViewModel
import com.lokatani.lokafreshinventory.data.local.ScanResultRepository

class HistoryViewModel(
    private val scanResultRepository: ScanResultRepository
) : ViewModel() {
    fun getAllResult() = scanResultRepository.getAllResult()
}