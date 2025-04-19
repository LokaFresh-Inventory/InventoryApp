package com.lokatani.lokafreshinventory.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lokatani.lokafreshinventory.data.local.entity.ScanResult

@Database(entities = [ScanResult::class], version = 1, exportSchema = false)
abstract class ScanResultDatabase : RoomDatabase() {
    abstract fun scanResultDao(): ScanResultDao

    companion object {
        @Volatile
        private var INSTANCE: ScanResultDatabase? = null
        fun getInstance(context: Context): ScanResultDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ScanResultDatabase::class.java, "ScanResult.db"
                )
                    .build()
            }
    }
}