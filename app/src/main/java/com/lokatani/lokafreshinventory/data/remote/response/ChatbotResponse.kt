package com.lokatani.lokafreshinventory.data.remote.response

import com.google.gson.annotations.SerializedName

data class ChatbotResponse(

    @field:SerializedName("output")
    val output: String? = null
)
