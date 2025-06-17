package com.lokatani.lokafreshinventory.data.remote.firebase

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val registrationDate: Timestamp = Timestamp.now()
)