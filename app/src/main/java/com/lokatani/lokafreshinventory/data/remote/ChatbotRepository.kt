package com.lokatani.lokafreshinventory.data.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.remote.response.ChatbotResponse
import com.lokatani.lokafreshinventory.data.remote.retrofit.ChatbotApiService

class ChatbotRepository(
    private val apiService: ChatbotApiService
) {
    fun sendChat(
        prompt: String
    ): LiveData<Result<ChatbotResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.sendChat(prompt)
            emit(Result.Success(response.first()))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ChatbotRepository? = null
        fun getInstance(apiService: ChatbotApiService): ChatbotRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChatbotRepository(apiService)
            }.also { INSTANCE = it }
    }
}