package com.lokatani.lokafreshinventory.ui.analysis

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.remote.PredictRepository
import com.lokatani.lokafreshinventory.data.remote.response.PredictResponse
import kotlinx.coroutines.launch

class AnalysisViewModel(private val predictRepository: PredictRepository) : ViewModel() {
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _predictionResult = MutableLiveData<Result<PredictResponse>>(null)
    val predictionResult: LiveData<Result<PredictResponse>> get() = _predictionResult

    fun predict(tanggal: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = predictRepository.predict(tanggal)
            _isLoading.value = false
            _predictionResult.value = result
        }
    }
}