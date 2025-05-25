package com.lokatani.lokafreshinventory.ui.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.local.entity.ScanResult
import kotlinx.coroutines.tasks.await

class DataViewModel : ViewModel() {
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