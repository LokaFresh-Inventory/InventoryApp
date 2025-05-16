package com.lokatani.lokafreshinventory.data.remote.response

import com.google.gson.annotations.SerializedName

data class OcrResponse(

    @field:SerializedName("texts")
    val texts: List<String?>? = null
)
