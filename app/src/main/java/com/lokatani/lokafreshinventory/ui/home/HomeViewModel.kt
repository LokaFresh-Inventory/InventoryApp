package com.lokatani.lokafreshinventory.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lokatani.lokafreshinventory.data.FirestoreRepository
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.remote.firebase.ScanResult
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.Calendar

class HomeViewModel(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _homeUiState = MutableLiveData<Result<HomeUiState>>()
    val homeUiState: LiveData<Result<HomeUiState>> = _homeUiState

    fun fetchDataForHomeScreen() {
        viewModelScope.launch {
            _homeUiState.value = Result.Loading

            // 1. Fetch the firestore data
            when (val historyResult = firestoreRepository.getScanHistory()) {
                is Result.Success -> {
                    val allScans = historyResult.data
                    if (allScans.isNotEmpty()) {
                        // 2. Process the data for multiple function
                        val totalWeights = processTotalWeights(allScans)
                        val monthlyData = processForMonthlyChart(allScans)
                        val lastInput = allScans.firstOrNull()

                        // 3. Combine into a single state object
                        val uiState = HomeUiState(
                            monthlyChartData = monthlyData,
                            totalWeights = totalWeights,
                            lastInput = lastInput
                        )
                        _homeUiState.value = Result.Success(uiState) // Post to Success State
                    } else {
                        _homeUiState.value = Result.Success(
                            HomeUiState(emptyMap(), emptyMap(), null)
                        )
                    }
                }

                is Result.Error -> {
                    _homeUiState.value = Result.Error(historyResult.error)
                }

                is Result.Loading -> {
                    // Processed by Fragment UI
                }
            }
        }
    }

    private fun processTotalWeights(scans: List<ScanResult>): Map<String, Int> {
        return scans
            .groupBy { it.vegResult }
            .mapValues { entry ->
                entry.value.sumOf { it.vegWeight }
            }
    }

    private fun processForMonthlyChart(scans: List<ScanResult>): Map<String, Map<String, Int>> {
        val monthlyAggregations = mutableMapOf<YearMonth, MutableMap<String, Int>>()

        for (scan in scans) {
            val vegType = scan.vegResult
            val vegWeight = scan.vegWeight
            val timestamp = scan.date

            if (vegType.isBlank() || timestamp == null) continue

            // Convert Timestamp to YearMonth
            val calendar = Calendar.getInstance()
            calendar.time = timestamp.toDate()
            val yearMonth = YearMonth.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1
            )

            monthlyAggregations
                .getOrPut(yearMonth) { mutableMapOf() }
                .merge(vegType, vegWeight, Int::plus)
        }
        return monthlyAggregations.mapKeys { it.key.toString() }
    }

    data class HomeUiState(
        val monthlyChartData: Map<String, Map<String, Int>>,
        val totalWeights: Map<String, Int>,
        val lastInput: ScanResult?
    )
}