package com.lokatani.lokafreshinventory.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import androidx.room.Update
import com.lokatani.lokafreshinventory.data.local.entity.ScanResult

@Dao
interface ScanResultDao {
    @Insert(onConflict = IGNORE)
    suspend fun insertScanResult(scanResult: ScanResult)

    @Delete
    suspend fun deleteScanResult(scanResult: ScanResult)

    @Query("SELECT * FROM scan_results")
    fun getAllResult(): LiveData<List<ScanResult>>

    @Query("SELECT * FROM scan_results WHERE firestoreId IS NULL")
    suspend fun getAllResultWithoutFirestoreId(): List<ScanResult>

    @Update
    suspend fun updateScanResult(scanResult: ScanResult)
}