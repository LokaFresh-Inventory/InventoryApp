package com.lokatani.lokafreshinventory.utils.tableview.holder

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.utils.tableview.model.Cell

class CellViewHolder(itemView: View) : AbstractViewHolder(itemView) {
    private val cellTextView: TextView = itemView.findViewById(R.id.cell_data)
    private val cellContainer: LinearLayout = itemView.findViewById(R.id.cell_container)

    fun setCell(cell: Cell?) {
        cellTextView.text = cell?.getData()?.toString() ?: ""

        // If your TableView should have auto resize for cells & columns.
        // Then you should consider the below lines. Otherwise, you can ignore them.
        cellContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        cellTextView.requestLayout()
    }
}