package com.lokatani.lokafreshinventory.di

import com.lokatani.lokafreshinventory.data.remote.CnnRepository
import com.lokatani.lokafreshinventory.data.remote.retrofit.ApiConfig

object CnnInjection {
    fun provideRepository(): CnnRepository {
        val apiService = ApiConfig.getCNNApiService()
        return CnnRepository.getInstance(apiService)
    }
}