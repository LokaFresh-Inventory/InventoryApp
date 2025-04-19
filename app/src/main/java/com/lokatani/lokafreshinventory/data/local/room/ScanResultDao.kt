package com.lokatani.lokafreshinventory.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import com.lokatani.lokafreshinventory.data.local.entity.ScanResult

@Dao
interface ScanResultDao {
    @Insert(onConflict = IGNORE)
    suspend fun insertScanResult(scanResult: ScanResult)

    @Query("SELECT * FROM scan_results")
    fun getAllResult(): LiveData<List<ScanResult>>
}