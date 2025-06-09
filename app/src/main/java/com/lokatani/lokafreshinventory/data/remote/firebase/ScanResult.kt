package com.lokatani.lokafreshinventory.data.remote.firebase

import com.google.firebase.Timestamp

data class ScanResult(
    val user: String = "",
    val vegResult: String = "",
    val vegWeight: Int = 0,
    val date: Timestamp? = null
)