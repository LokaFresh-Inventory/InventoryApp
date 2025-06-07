package com.lokatani.lokafreshinventory.utils

import android.content.Context
import android.widget.Toast
import com.lokatani.lokafreshinventory.utils.tableview.model.Cell
import com.lokatani.lokafreshinventory.utils.tableview.model.ColumnHeader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun escapeCsv(value: String?): String {
    val strValue = value ?: ""
    if (strValue.contains(",") || strValue.contains("\"") || strValue.contains("\n") || strValue.contains(
            "\r"
        )
    ) {
        return "\"${strValue.replace("\"", "\"\"")}\""
    }
    return strValue
}

fun generateCsvString(
    columnHeaders: List<ColumnHeader>,
    filteredCellData: List<List<Cell>> // This is List<List<Cell>> from tableView.getFilteredList()
): String {
    val csvBuilder = StringBuilder()
    // Define the date formatter for CSV output (e.g., YYYY-MM-DD is good for compatibility)
    val csvDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

    // Append Header Row
    csvBuilder.appendLine(columnHeaders.joinToString(",") { header ->
        escapeCsv(header.getData()?.toString())
    })

    // Append Data Rows
    for (rowCells in filteredCellData) {
        csvBuilder.appendLine(rowCells.joinToString(",") { cell ->
            val data = cell.getData()
            val stringValue = when (data) {
                is LocalDate -> data.format(csvDateFormatter) // Format date for CSV
                // Add more custom formatting for other types if needed (e.g., numbers)
                else -> data?.toString() // Default toString for others (String, Int, etc.)
            }
            escapeCsv(stringValue)
        })
    }
    return csvBuilder.toString()
}