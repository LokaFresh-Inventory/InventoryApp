package com.lokatani.lokafreshinventory.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lokatani.lokafreshinventory.data.remote.PredictRepository
import com.lokatani.lokafreshinventory.di.PredictInjection

class PredictViewModelFactory private constructor(
    private val predictRepository: PredictRepository,
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AnalysisViewModel::class.java) -> {
                return AnalysisViewModel(predictRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown viewmodel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PredictViewModelFactory? = null
        fun getInstance(): PredictViewModelFactory =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PredictViewModelFactory(PredictInjection.provideRepository())
            }.also { INSTANCE = it }
    }
}