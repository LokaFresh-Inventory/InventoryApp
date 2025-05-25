package com.lokatani.lokafreshinventory.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.lokatani.lokafreshinventory.data.local.ScanResultRepository
import com.lokatani.lokafreshinventory.data.local.room.ScanResultDatabase

object ScanResultInjection {
    fun provideRepository(context: Context): ScanResultRepository {
        val database = ScanResultDatabase.getInstance(context)
        val scanResultDao = database.scanResultDao()
        val firestore = FirebaseFirestore.getInstance()

        val scanResultRepository = ScanResultRepository.getInstance(scanResultDao, firestore)
        return scanResultRepository
    }
}