package com.lokatani.lokafreshinventory.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.lokatani.lokafreshinventory.data.remote.firebase.MonthlyVegData
import com.lokatani.lokafreshinventory.data.remote.firebase.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Locale

class FirestoreRepository private constructor(
    private val firestore: FirebaseFirestore
) {
    // Function to insert a new scan result into Firestore
    suspend fun insertScanResult(scanResult: ScanResult): Result<Unit> { // Return Result<Unit> for success/failure indication
        return withContext(Dispatchers.IO) {
            try {
                // "db-scan-lokatani" is the collection name you are using in getMonthlyVegWeightDataFromFirestore
                firestore.collection("db-scan-lokatani")
                    .add(scanResult) // Firestore will auto-generate a document ID
                    .await() // Wait for the operation to complete
                Log.d("FirestoreData", "DocumentSnapshot added successfully")
                Result.Success(Unit) // Indicate success
            } catch (e: Exception) {
                Log.e("FirestoreData", "Error adding document to Firestore", e)
                Result.Error("Error adding scan result: ${e.message}") // Indicate failure
            }
        }
    }

    suspend fun getMonthlyVegWeightDataFromFirestore(): Result<List<MonthlyVegData>> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = firestore.collection("db-scan-lokatani")
                    // Optional: Order by date if you want a consistent order for processing
                    // .orderBy("date", Query.Direction.ASCENDING) // Make sure you have an index for this if you use it in prod
                    .get()
                    .await()

                val monthlyAggregations = mutableMapOf<YearMonth, MutableMap<String, Int>>()

                // Define the possible date formats from your database
                val inputFormat1 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val inputFormat2 =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

                for (document in querySnapshot.documents) {
                    val vegType = document.getString("vegResult")
                    val vegWeight =
                        document.getLong("vegWeight")?.toInt() // Firestore Long to Kotlin Int
                    val dateString = document.getString("date")

                    if (vegType == null || vegWeight == null || dateString == null) {
                        Log.w(
                            "FirestoreData",
                            "Skipping document with missing fields: ${document.id}"
                        )
                        continue
                    }

                    var yearMonth: YearMonth? = null

                    // Try parsing dd/MM/yyyy first
                    try {
                        val date = inputFormat1.parse(dateString)
                        if (date != null) {
                            val calendar = Calendar.getInstance()
                            calendar.time = date
                            yearMonth = YearMonth.of(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) + 1
                            )
                        }
                    } catch (e: ParseException) {
                        // Ignore, try next format
                    }

                    // If first parse failed, try yyyy-MM-dd'T'HH:mm:ss
                    if (yearMonth == null) {
                        try {
                            val localDateTime = LocalDateTime.parse(dateString, inputFormat2)
                            yearMonth = YearMonth.from(localDateTime)
                        } catch (e: DateTimeParseException) {
                            Log.e(
                                "FirestoreData",
                                "Failed to parse date: $dateString for document: ${document.id}",
                                e
                            )
                            continue // Skip this entry if date cannot be parsed
                        }
                    }

                    if (yearMonth != null) {
                        monthlyAggregations
                            .getOrPut(yearMonth) { mutableMapOf() }
                            .merge(vegType, vegWeight) { oldVal, newVal -> oldVal + newVal }
                    }
                }

                val resultList = mutableListOf<MonthlyVegData>()
                for ((ym, vegData) in monthlyAggregations) {
                    for ((vegType, totalWeight) in vegData) {
                        resultList.add(MonthlyVegData(ym.toString(), vegType, totalWeight))
                    }
                }
                return@withContext Result.Success(resultList.sortedBy { it.yearMonth }) // Sort for consistent order

            } catch (e: Exception) {
                Log.e(
                    "FirestoreData",
                    "Error fetching or processing Firestore data: ${e.message}",
                    e
                )
                return@withContext Result.Error(
                    e.message ?: "Unknown error fetching chart data from Firestore"
                )
            }
        }
    }


    companion object {
        @Volatile
        private var INSTANCE: FirestoreRepository? = null
        fun getInstance(
            firestore: FirebaseFirestore
        ): FirestoreRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreRepository(firestore)
            }.also { INSTANCE = it }
    }
}