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

    private val _userListForFilter = MutableLiveData<List<String>>()
    val userListForFilter: LiveData<List<String>> = _userListForFilter

    private val _vegetableListForFilter = MutableLiveData<List<String>>()
    val vegetableListForFilter: LiveData<List<String>> = _vegetableListForFilter

    private val _currentFilterState = MutableLiveData(FilterState())
    val currentFilterState: LiveData<FilterState> = _currentFilterState

    fun prepareFilterData(scanResults: List<ScanResult>) {
        val users = scanResults
            .map { it.user }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .toMutableList()
        _userListForFilter.value = users

        val vegetables = scanResults
            .map { it.vegResult }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .toMutableList()
        _vegetableListForFilter.value = vegetables
    }

    fun applyFilters(
        user: String?,
        vegetable: String?,
        minWeight: Float?,
        maxWeight: Float?,
        allText: String
    ) {
        val userFilter = if (user.isNullOrBlank() || user == allText) null else user
        val vegFilter = if (vegetable.isNullOrBlank() || vegetable == allText) null else vegetable

        _currentFilterState.value = FilterState(
            user = userFilter,
            vegetable = vegFilter,
            minWeight = minWeight,
            maxWeight = maxWeight
        )
    }

    fun clearUserFilters(
        vegetable: String?,
        allText: String,
        minWeight: Float?,
        maxWeight: Float?
    ) {
        val vegFilter = if (vegetable.isNullOrBlank() || vegetable == allText) null else vegetable
        _currentFilterState.value = FilterState(
            user = null,
            vegetable = vegFilter,
            minWeight = minWeight,
            maxWeight = maxWeight
        )
    }

    fun clearVegetableFilters(
        user: String?,
        allText: String,
        minWeight: Float?,
        maxWeight: Float?
    ) {
        val userFilter = if (user.isNullOrBlank() || user == allText) null else user
        _currentFilterState.value = FilterState(
            user = userFilter,
            vegetable = null,
            minWeight = minWeight,
            maxWeight = maxWeight
        )
    }

    data class FilterState(
        val user: String? = null,
        val vegetable: String? = null,
        val minWeight: Float? = null,
        val maxWeight: Float? = null
    )
}