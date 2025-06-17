package com.lokatani.lokafreshinventory.data.remote.response

import com.google.gson.annotations.SerializedName

data class CnnResponse(

    @field:SerializedName("class_label")
    val classLabel: String? = null,

    @field:SerializedName("probabilities")
    val probabilities: List<Any?>? = null
)
