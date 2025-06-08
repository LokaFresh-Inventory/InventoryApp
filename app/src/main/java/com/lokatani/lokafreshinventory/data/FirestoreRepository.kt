package com.lokatani.lokafreshinventory.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lokatani.lokafreshinventory.data.remote.firebase.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreRepository private constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun insertScanResult(scanResult: ScanResult): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                firestore.collection("db-scan-lokatani")
                    .add(scanResult)
                    .await()
                Log.d("FirestoreData", "DocumentSnapshot added successfully")
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e("FirestoreData", "Error adding document to Firestore", e)
                Result.Error("Error adding scan result: ${e.message}")
            }
        }
    }

    suspend fun getScanHistory(): Result<List<ScanResult>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("db-scan-lokatani")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val results = snapshot.toObjects(ScanResult::class.java)
                return@withContext Result.Success(results)

            } catch (e: Exception) {
                Log.e("FirestoreData", "Error fetching scan history: ${e.message}", e)
                return@withContext Result.Error(e.message ?: "Unknown error fetching history")
            }
        }
    }


    companion object {
        @Volatile
        private var INSTANCE: FirestoreRepository? = null
        fun getInstance(
            firestore: FirebaseFirestore
        ): FirestoreRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreRepository(firestore)
            }.also { INSTANCE = it }
    }
}