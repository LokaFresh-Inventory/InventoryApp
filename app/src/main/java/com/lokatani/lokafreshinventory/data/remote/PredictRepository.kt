package com.lokatani.lokafreshinventory.data.remote

import com.google.gson.Gson
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.remote.request.PredictRequest
import com.lokatani.lokafreshinventory.data.remote.response.DetailItem
import com.lokatani.lokafreshinventory.data.remote.response.PredictResponse
import com.lokatani.lokafreshinventory.data.remote.retrofit.ApiService
import retrofit2.HttpException

class PredictRepository(
    private val apiService: ApiService
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
            val errorBody = Gson().fromJson(jsonInString, DetailItem::class.java)
            Result.Error(errorBody.msg ?: "Unknown Error Occured")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown Error Occured")
        }
    }

    companion object {
        fun getInstance(
            apiService: ApiService
        ): PredictRepository = PredictRepository(apiService)
    }
}