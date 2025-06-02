package com.lokatani.lokafreshinventory.di

import com.google.firebase.firestore.FirebaseFirestore
import com.lokatani.lokafreshinventory.data.FirestoreRepository

object FirestoreInjection {
    fun provideRepository(): FirestoreRepository {
        val firestore = FirebaseFirestore.getInstance()

        val firestoreRepository = FirestoreRepository.getInstance(firestore)
        return firestoreRepository
    }
}