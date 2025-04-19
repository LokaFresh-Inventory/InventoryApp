package com.lokatani.lokafreshinventory.ui.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lokatani.lokafreshinventory.data.local.ScanResultRepository
import com.lokatani.lokafreshinventory.data.local.entity.ScanResult
import kotlinx.coroutines.launch

class ScanViewModel(
    private val scanResultRepository: ScanResultRepository
) : ViewModel() {
    private val _insertCompleted = MutableLiveData<Boolean>()
    val insertCompleted: LiveData<Boolean> get() = _insertCompleted

    fun insertResult(
        user: String,
        vegResult: String,
        vegWeight: Float,
        date: String
    ) {
        val result = ScanResult(
            user = user,
            vegResult = vegResult,
            vegWeight = vegWeight,
            date = date
        )

        viewModelScope.launch {
            scanResultRepository.insertScanResult(result)
            _insertCompleted.value = true
        }
    }

    fun resetInsertStatus() {
        _insertCompleted.value = false
    }
}