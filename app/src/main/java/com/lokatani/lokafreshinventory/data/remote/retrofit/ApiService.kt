package com.lokatani.lokafreshinventory.data.remote.retrofit

import com.lokatani.lokafreshinventory.data.remote.request.PredictRequest
import com.lokatani.lokafreshinventory.data.remote.response.PredictResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("predict")
    suspend fun predict(
        @Body predictRequest: PredictRequest
    ): PredictResponse
}