package com.lokatani.lokafreshinventory.utils.tableview.holder

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.google.firebase.Timestamp
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.utils.tableview.model.Cell
import java.text.SimpleDateFormat
import java.util.Locale

class CellViewHolder(itemView: View) : AbstractViewHolder(itemView) {
    val cellContainer: LinearLayout = itemView.findViewById(R.id.cell_container)
    val cellTextView: TextView = itemView.findViewById(R.id.cell_data)

    private val displayDateFormatter =
        SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())

    fun setCell(cell: Cell?) {
        val data = cell?.getData()
        val displayText: String

        when (data) {
            is Timestamp -> {
                displayText = displayDateFormatter.format(data.toDate())
            }

            is Number -> {
                displayText = data.toString()
            }

            null -> {
                displayText = ""
            }

            else -> {
                displayText = data.toString()
            }
        }

        cellTextView.text = displayText

        cellContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        cellTextView.requestLayout()
    }
}