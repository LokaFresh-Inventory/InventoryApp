package com.lokatani.lokafreshinventory.ui.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lokatani.lokafreshinventory.data.FirestoreRepository
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.remote.firebase.MonthlyVegData
import com.lokatani.lokafreshinventory.data.remote.firebase.ScanResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HistoryViewModel(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {
    val monthlyVegData = MutableLiveData<Result<List<MonthlyVegData>>>()
    fun fetchMonthlyVegData() {
        viewModelScope.launch {
            monthlyVegData.value = Result.Loading // Indicate loading
            val result =
                firestoreRepository.getMonthlyVegWeightDataFromFirestore() // CALL THE FIRESTORE FUNCTION
            monthlyVegData.value = result // Update status
        }
    }

    private val firestore = Firebase.firestore
    private val _scanResults = MutableLiveData<Result<List<ScanResult>>>()
    val scanResults: LiveData<Result<List<ScanResult>>> = _scanResults

    // Function to fetch data from Firestore
    suspend fun fetchScanResults() {
        _scanResults.value = Result.Loading

        try {
            val snapshot = firestore.collection("db-scan-lokatani").get().await()
            val results = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(ScanResult::class.java)
                } catch (e: Exception) {
                    Log.e("Firestore", "Error converting document to ScanResult: ${e.message}", e)
                    null // Skip documents that fail conversion
                }
            }

            _scanResults.value = Result.Success(results)
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching scan results: ${e.message}", e)
            _scanResults.value = Result.Error("Error fetching data from Firestore: ${e.message}")
        }
    }
}