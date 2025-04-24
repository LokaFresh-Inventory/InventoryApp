package com.lokatani.lokafreshinventory.data.remote.response

import com.google.gson.annotations.SerializedName

data class PredictResponse(

    @field:SerializedName("bayam_merah")
    val bayamMerah: Int? = null,

    @field:SerializedName("tanggal")
    val tanggal: String? = null,

    @field:SerializedName("kale")
    val kale: Int? = null
)
