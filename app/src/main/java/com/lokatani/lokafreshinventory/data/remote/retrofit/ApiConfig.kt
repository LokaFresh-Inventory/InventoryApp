package com.lokatani.lokafreshinventory.data.remote.retrofit

import com.lokatani.lokafreshinventory.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    fun getPredictApiService(): PredictApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.MODEL_API_ENDPOINT) // Predict
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PredictApiService::class.java)
    }

    fun getChatbotApiService(): ChatbotApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.CHATBOT_API_ENDPOINT) // Chatbot
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatbotApiService::class.java)
    }

    fun getOCRApiService(): OcrApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.MODEL_API_ENDPOINT) // OCR
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OcrApiService::class.java)
    }

    fun getCNNApiService(): CnnApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.CNN_API_ENDPOINT) // CNN
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CnnApiService::class.java)
    }
}