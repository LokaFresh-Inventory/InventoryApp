package com.lokatani.lokafreshinventory.di

import com.lokatani.lokafreshinventory.data.remote.PredictRepository
import com.lokatani.lokafreshinventory.data.remote.retrofit.ApiConfig

object PredictInjection {
    fun provideRepository(): PredictRepository {
        val apiService = ApiConfig.getPredictApiService()
        return PredictRepository.getInstance(apiService)
    }
}