package com.lokatani.lokafreshinventory.utils

import java.text.SimpleDateFormat
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
}