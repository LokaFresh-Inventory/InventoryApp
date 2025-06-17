package com.lokatani.lokafreshinventory.data.remote

import android.graphics.Bitmap
import com.google.gson.Gson
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.remote.response.ApiHitResponse
import com.lokatani.lokafreshinventory.data.remote.response.CnnResponse
import com.lokatani.lokafreshinventory.data.remote.retrofit.CnnApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream

class CnnRepository(
    val apiService: CnnApiService
) {
    suspend fun recognizeVeggie(
        bitmap: Bitmap
    ): Result<CnnResponse> {
        return try {
            // Convert bitmap to file
            val file = convertBitmapToFile(bitmap)

            // Create MultipartBody.Part from file
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            // Send the request
            val response = apiService.sendVegImage(imagePart)
            Result.Success(response)
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ApiHitResponse::class.java)
            Result.Error(errorBody.msg ?: "Unknown Error Occurred")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown Error Occurred")
        }
    }

    private fun convertBitmapToFile(bitmap: Bitmap): File {
        // Create a file in the cache directory
        val file = File.createTempFile("image", ".png")

        // Convert bitmap to file
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
        }

        return file
    }

    companion object {
        @Volatile
        private var INSTANCE: CnnRepository? = null
        fun getInstance(apiService: CnnApiService): CnnRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CnnRepository(apiService)
            }.also { INSTANCE = it }
    }
}