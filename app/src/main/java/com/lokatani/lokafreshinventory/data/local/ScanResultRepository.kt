package com.lokatani.lokafreshinventory.data.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.local.entity.ScanResult
import com.lokatani.lokafreshinventory.data.local.room.ScanResultDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScanResultRepository private constructor(
    private val scanResultDao: ScanResultDao
) {
    fun getAllResult(): LiveData<Result<List<ScanResult>>> = liveData {
        emit(Result.Loading)
        val localData: LiveData<Result<List<ScanResult>>> =
            scanResultDao.getAllResult().map { Result.Success(it) }
        emitSource(localData)
    }

    suspend fun insertScanResult(scanResult: ScanResult) {
        withContext(Dispatchers.IO) {
            scanResultDao.insertScanResult(scanResult)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ScanResultRepository? = null
        fun getInstance(
            scanResultDao: ScanResultDao
        ): ScanResultRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScanResultRepository(scanResultDao)
            }.also { INSTANCE = it }
    }
}