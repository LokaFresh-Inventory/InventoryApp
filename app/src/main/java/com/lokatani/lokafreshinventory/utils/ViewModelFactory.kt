package com.lokatani.lokafreshinventory.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lokatani.lokafreshinventory.data.local.ScanResultRepository
import com.lokatani.lokafreshinventory.di.ScanResultInjection
import com.lokatani.lokafreshinventory.ui.detail.DetailViewModel
import com.lokatani.lokafreshinventory.ui.history.HistoryViewModel

class ViewModelFactory private constructor(
    private val scanResultRepository: ScanResultRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(scanResultRepository) as T
            }

            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(scanResultRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown viewmodel class: " + modelClass.name)
        }
    }

    companion object {
        fun getInstance(context: Context): ViewModelFactory {
            val scanResultRepository = ScanResultInjection.provideRepository(context)
            return ViewModelFactory(scanResultRepository)
        }
    }
}