package com.lokatani.lokafreshinventory.data.remote.firebase

data class MonthlyVegData(
    val yearMonth: String, // e.g., "2025-01"
    val vegType: String,
    val totalWeight: Int
)