package com.lokatani.lokafreshinventory.di

import com.lokatani.lokafreshinventory.data.remote.OcrRepository
import com.lokatani.lokafreshinventory.data.remote.retrofit.ApiConfig

object OcrInjection {
    fun provideRepository(): OcrRepository {
        val apiService = ApiConfig.getOCRApiService()
        return OcrRepository.getInstance(apiService)
    }
}