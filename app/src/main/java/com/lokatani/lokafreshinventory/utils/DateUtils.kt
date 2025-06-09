package com.lokatani.lokafreshinventory.utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDate(dateString: String, locale: Locale = Locale.getDefault()): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", locale)

        val date = inputFormat.parse(dateString)

        val outputFormat: SimpleDateFormat = when (locale.language) {
            "in" -> SimpleDateFormat("EEEE, d MMMM yyyy", locale)
            else -> SimpleDateFormat("EEEE, MMMM d'th' yyyy", locale)
        }

        return outputFormat.format(date!!)
    }

    fun timeStamp(): String {

        val timeStamp = Timestamp(System.currentTimeMillis())
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = sdf.format(Date(timeStamp.time))

        return time.toString()
    }
}