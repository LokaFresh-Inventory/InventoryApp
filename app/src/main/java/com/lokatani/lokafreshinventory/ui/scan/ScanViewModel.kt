package com.lokatani.lokafreshinventory.ui.scan

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.remote.OcrRepository
import com.lokatani.lokafreshinventory.data.remote.response.OcrResponse
import kotlinx.coroutines.launch

class ScanViewModel(
    private val ocrRepository: OcrRepository
) : ViewModel() {
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _ocrResult = MutableLiveData<Result<OcrResponse>>(null)
    val ocrResult: LiveData<Result<OcrResponse>> get() = _ocrResult

    fun recognizeText(bitmap: Bitmap) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = ocrRepository.recognizeText(bitmap)
            _isLoading.value = false
            _ocrResult.value = result
        }
    }
}