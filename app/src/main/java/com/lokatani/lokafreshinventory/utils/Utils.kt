package com.lokatani.lokafreshinventory.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.Timestamp
import com.lokatani.lokafreshinventory.utils.tableview.model.Cell
import com.lokatani.lokafreshinventory.utils.tableview.model.ColumnHeader
import java.text.SimpleDateFormat
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
    filteredCellData: List<List<Cell>>
): String {
    val csvBuilder = StringBuilder()
    val csvDateFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))

    // 1. Append Header Row
    val headerRow = columnHeaders.joinToString(",") { header ->
        escapeCsv(header.getData()?.toString())
    }
    csvBuilder.appendLine(headerRow)

    // 2. Append Data Rows
    for (rowCells in filteredCellData) {
        val rowString = rowCells.joinToString(",") { cell ->
            val data = cell.getData()

            val stringValue = when (data) {
                is Timestamp -> {
                    csvDateFormatter.format(data.toDate())
                }

                else -> {
                    data?.toString() ?: ""
                }
            }
            escapeCsv(stringValue)
        }
        csvBuilder.appendLine(rowString)
    }
    return csvBuilder.toString()
}