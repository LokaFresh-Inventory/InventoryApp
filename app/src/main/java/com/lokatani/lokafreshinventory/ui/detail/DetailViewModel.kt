package com.lokatani.lokafreshinventory.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lokatani.lokafreshinventory.data.FirestoreRepository
import com.lokatani.lokafreshinventory.data.remote.firebase.ScanResult
import kotlinx.coroutines.launch

class DetailViewModel(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {
    private val _insertCompleted = MutableLiveData<Boolean>()
    val insertCompleted: LiveData<Boolean> get() = _insertCompleted

    fun insertResult(
        user: String,
        vegResult: String,
        vegWeight: Int,
        date: String
    ) {
        val result = ScanResult(
            user = user,
            vegResult = vegResult,
            vegWeight = vegWeight,
            date = date
        )

        viewModelScope.launch {
            firestoreRepository.insertScanResult(result)
            _insertCompleted.value = true
        }
    }

    fun resetInsertStatus() {
        _insertCompleted.value = false
    }
}