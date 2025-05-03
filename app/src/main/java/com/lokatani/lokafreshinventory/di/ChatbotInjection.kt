package com.lokatani.lokafreshinventory.di

import com.lokatani.lokafreshinventory.data.remote.ChatbotRepository
import com.lokatani.lokafreshinventory.data.remote.retrofit.ApiConfig

object ChatbotInjection {
    fun provideRepository(): ChatbotRepository {
        val apiService = ApiConfig.getChatbotApiService()
        return ChatbotRepository.getInstance(apiService)
    }
}