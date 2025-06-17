package com.lokatani.lokafreshinventory.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lokatani.lokafreshinventory.data.remote.CnnRepository
import com.lokatani.lokafreshinventory.data.remote.OcrRepository
import com.lokatani.lokafreshinventory.di.CnnInjection
import com.lokatani.lokafreshinventory.di.OcrInjection

class ScanViewModelFactory private constructor(
    private val ocrRepository: OcrRepository,
    private val cnnRepository: CnnRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ScanViewModel::class.java) -> {
                return ScanViewModel(ocrRepository, cnnRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown viewmodel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ScanViewModelFactory? = null
        fun getInstance(): ScanViewModelFactory =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScanViewModelFactory(
                    OcrInjection.provideRepository(),
                    CnnInjection.provideRepository()
                )
            }.also { INSTANCE = it }
    }
}