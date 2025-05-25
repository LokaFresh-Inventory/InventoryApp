package com.lokatani.lokafreshinventory.data.local

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.google.firebase.firestore.FirebaseFirestore
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.local.entity.FirestoreScanResult
import com.lokatani.lokafreshinventory.data.local.entity.ScanResult
import com.lokatani.lokafreshinventory.data.local.room.ScanResultDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ScanResultRepository private constructor(
    private val scanResultDao: ScanResultDao,
    private val firestore: FirebaseFirestore
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

    suspend fun deleteScanResult(scanResult: ScanResult) {
        withContext(Dispatchers.IO) {
            scanResultDao.deleteScanResult((scanResult))
        }
    }

    suspend fun syncLocalToFirestore(): Result<Unit> {
        val newScanResults = scanResultDao.getAllResultWithoutFirestoreId()
        if (newScanResults.isEmpty()) {
            return Result.Success(Unit) // Nothing to sync
        }

        for (scanResult in newScanResults) {
            try {
                // Convert the Room ScanResult to the Firestore-specific DTO
                val firestoreScanResult = FirestoreScanResult.fromScanResult(scanResult)

                // Add the DTO to Firestore
                val documentRef =
                    firestore.collection("db-scan-lokatani").add(firestoreScanResult).await()

                // Update the local Room entry with the Firestore ID
                scanResult.firestoreId = documentRef.id
                scanResultDao.updateScanResult(scanResult)

            } catch (e: Exception) {
                Log.e("FirestoreSync", "Error adding scan result to Firestore: ${e.message}")
                return Result.Error("Failed to sync some data: ${e.message}")
            }
        }
        return Result.Success(Unit)
    }

    companion object {
        @Volatile
        private var INSTANCE: ScanResultRepository? = null
        fun getInstance(
            scanResultDao: ScanResultDao,
            firestore: FirebaseFirestore
        ): ScanResultRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScanResultRepository(scanResultDao, firestore)
            }.also { INSTANCE = it }
    }
}