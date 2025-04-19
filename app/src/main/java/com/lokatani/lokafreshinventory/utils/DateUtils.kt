package com.lokatani.lokafreshinventory.utils

import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

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

    fun formatLocalDateToRelativeTime(
        dateString: String,
        locale: Locale = Locale.getDefault()
    ): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
        val parsedDate = LocalDate.parse(dateString, dateFormatter)

        val parsedDateTime = parsedDate.atStartOfDay(ZoneId.systemDefault())
        val now = ZonedDateTime.now()
        val duration = ChronoUnit.SECONDS.between(parsedDateTime, now)

        return when {
            duration < 60 -> "${duration}s ago"
            duration < 3600 -> "${TimeUnit.SECONDS.toMinutes(duration)}m ago"
            duration < 86400 -> "${TimeUnit.SECONDS.toHours(duration)}h ago"
            else -> "${TimeUnit.SECONDS.toDays(duration)}d ago"
        }
    }
}