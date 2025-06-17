package com.lokatani.lokafreshinventory.ui.scan

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.remote.CnnRepository
import com.lokatani.lokafreshinventory.data.remote.OcrRepository
import com.lokatani.lokafreshinventory.data.remote.response.OcrResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class ScanUiState(
    val vegetableName: String?,
    val weight: Int?
)

class ScanViewModel(
    private val ocrRepository: OcrRepository,
    private val cnnRepository: CnnRepository
) : ViewModel() {
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _scanResult = MutableLiveData<Result<ScanUiState>>()
    val scanResult: LiveData<Result<ScanUiState>> get() = _scanResult

    // OCR Only
    fun recognizeText(bitmap: Bitmap) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val ocrResult = ocrRepository.recognizeText(bitmap)) {
                is Result.Success -> {
                    val weight = extractWeightFromOcr(ocrResult.data)
                    _scanResult.value =
                        Result.Success(ScanUiState(vegetableName = null, weight = weight))
                }

                is Result.Error -> {
                    _scanResult.value = Result.Error(ocrResult.error)
                }

                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Vegetable Only
    fun recognizeVeggie(bitmap: Bitmap) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val cnnResult = cnnRepository.recognizeVeggie(bitmap)) {
                is Result.Success -> {
                    val vegName = cnnResult.data.classLabel
                    _scanResult.value = Result.Success(
                        ScanUiState(
                            vegetableName = "Hasil API: $vegName",
                            weight = null
                        )
                    )
                }

                is Result.Error -> {
                    _scanResult.value = Result.Error(cnnResult.error)
                }

                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Both OCR and Vegetable
    fun recognizeBoth(veggieBitmap: Bitmap, scaleBitmap: Bitmap) {
        viewModelScope.launch {
            _isLoading.value = true

            val cnnDeferred = async { cnnRepository.recognizeVeggie(veggieBitmap) }
            val ocrDeferred = async { ocrRepository.recognizeText(scaleBitmap) }

            val cnnResult = cnnDeferred.await()
            val ocrResult = ocrDeferred.await()

            when {
                cnnResult is Result.Error -> {
                    _scanResult.value = Result.Error(cnnResult.error)
                }

                ocrResult is Result.Error -> {
                    _scanResult.value = Result.Error(ocrResult.error)
                }

                cnnResult is Result.Success && ocrResult is Result.Success -> {
                    val vegName = cnnResult.data.classLabel
                    val weight = extractWeightFromOcr(ocrResult.data)
                    _scanResult.value = Result.Success(
                        ScanUiState(
                            vegetableName = "Hasil API: $vegName",
                            weight = weight
                        )
                    )
                }
            }
            _isLoading.value = false
        }
    }

    private fun extractWeightFromOcr(ocrResponse: OcrResponse): Int? {
        val rawTexts = ocrResponse.texts
        if (rawTexts.isNullOrEmpty()) return null

        val combinedText = rawTexts.joinToString("") { it!!.trim() }
        val numberRegex = "-?\\d+(\\.\\d+)?".toRegex()
        val foundNumbers = numberRegex.findAll(combinedText).map { it.value }.toList()
        val potentialWeights = foundNumbers.mapNotNull { it.toIntOrNull() }
        return potentialWeights.firstOrNull()
    }
}