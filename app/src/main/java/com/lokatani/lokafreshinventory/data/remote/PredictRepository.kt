package com.lokatani.lokafreshinventory.data.remote

import com.google.gson.Gson
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.remote.request.PredictRequest
import com.lokatani.lokafreshinventory.data.remote.response.ApiHitResponse
import com.lokatani.lokafreshinventory.data.remote.response.PredictResponse
import com.lokatani.lokafreshinventory.data.remote.retrofit.PredictApiService
import retrofit2.HttpException

class PredictRepository(
    private val apiService: PredictApiService
) {
    suspend fun predict(
        tanggal: String
    ): Result<PredictResponse> {
        return try {
            val request = PredictRequest(tanggal)
            val response = apiService.predict(request)
            Result.Success(response)
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ApiHitResponse::class.java)
            Result.Error(errorBody.msg ?: "Unknown Error Occured")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown Error Occured")
        }
    }

    companion object {
        fun getInstance(
            apiService: PredictApiService
        ): PredictRepository = PredictRepository(apiService)
    }
}