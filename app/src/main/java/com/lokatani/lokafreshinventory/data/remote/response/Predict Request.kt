package com.lokatani.lokafreshinventory.data.remote.request

import com.google.gson.annotations.SerializedName

data class PredictRequest(
    @SerializedName("tanggal")
    val tanggal: String
)