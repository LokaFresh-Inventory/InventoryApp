package com.lokatani.lokafreshinventory.ui.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.ScanResultRepository
import com.lokatani.lokafreshinventory.data.local.entity.ScanResult
import com.lokatani.lokafreshinventory.data.remote.firebase.MonthlyVegData
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

    val syncStatus = MutableLiveData<Result<Unit>>() // Or a more specific status
    fun syncDataToFirestore() {
        viewModelScope.launch {
            syncStatus.value = Result.Loading // Indicate loading
            val result = scanResultRepository.syncLocalToFirestore()
            syncStatus.value = result // Update status
        }
    }

    val monthlyVegData = MutableLiveData<Result<List<MonthlyVegData>>>()
    fun fetchMonthlyVegData() {
        viewModelScope.launch {
            monthlyVegData.value = Result.Loading // Indicate loading
            val result =
                scanResultRepository.getMonthlyVegWeightDataFromFirestore() // CALL THE FIRESTORE FUNCTION
            monthlyVegData.value = result // Update status
        }
    }
}