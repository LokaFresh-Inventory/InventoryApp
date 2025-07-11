package com.lokatani.lokafreshinventory.data.remote.response

import com.google.gson.annotations.SerializedName

data class ApiHitResponse(

    @field:SerializedName("msg")
    val msg: String? = null,

    @field:SerializedName("loc")
    val loc: List<String?>? = null,

    @field:SerializedName("input")
    val input: Any? = null,

    @field:SerializedName("type")
    val type: String? = null
)