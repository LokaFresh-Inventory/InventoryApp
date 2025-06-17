package com.lokatani.lokafreshinventory.data.remote.retrofit

import com.lokatani.lokafreshinventory.data.remote.request.PredictRequest
import com.lokatani.lokafreshinventory.data.remote.response.ChatbotResponse
import com.lokatani.lokafreshinventory.data.remote.response.CnnResponse
import com.lokatani.lokafreshinventory.data.remote.response.OcrResponse
import com.lokatani.lokafreshinventory.data.remote.response.PredictResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface PredictApiService {
    @POST("predict")
    suspend fun predict(
        @Body predictRequest: PredictRequest
    ): PredictResponse
}

interface ChatbotApiService {
    @POST("webhook/get-prompt")
    suspend fun sendChat(
        @Query("prompt") prompt: String
    ): List<ChatbotResponse>
}

interface OcrApiService {
    @Multipart
    @POST("ocr")
    suspend fun sendImage(
        @Part file: MultipartBody.Part
    ): OcrResponse
}

interface CnnApiService {
    @Multipart
    @POST("cnn")
    suspend fun sendVegImage(
        @Part file: MultipartBody.Part
    ): CnnResponse
}