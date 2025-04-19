package com.lokatani.lokafreshinventory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResult(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo
    val id: Int = 0,

    @ColumnInfo
    val user: String,

    @ColumnInfo
    val vegResult: String,

    @ColumnInfo
    val vegWeight: Float,

    @ColumnInfo
    val date: String
)
