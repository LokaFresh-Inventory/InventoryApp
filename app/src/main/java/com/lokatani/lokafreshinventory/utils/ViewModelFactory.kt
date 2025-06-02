package com.lokatani.lokafreshinventory.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lokatani.lokafreshinventory.data.FirestoreRepository
import com.lokatani.lokafreshinventory.di.FirestoreInjection
import com.lokatani.lokafreshinventory.ui.detail.DetailViewModel
import com.lokatani.lokafreshinventory.ui.history.HistoryViewModel

class ViewModelFactory private constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(firestoreRepository) as T
            }

            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(firestoreRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown viewmodel class: " + modelClass.name)
        }
    }

    companion object {
        fun getInstance(): ViewModelFactory {
            val firestoreRepository = FirestoreInjection.provideRepository()
            return ViewModelFactory(firestoreRepository)
        }
    }
}