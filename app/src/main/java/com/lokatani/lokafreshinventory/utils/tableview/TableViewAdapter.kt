package com.lokatani.lokafreshinventory.utils.tableview

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.evrencoskun.tableview.sort.SortState
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.utils.tableview.holder.ColumnHeaderViewHolder // Import your ColumnHeaderViewHolder
import com.lokatani.lokafreshinventory.utils.tableview.model.Cell
import com.lokatani.lokafreshinventory.utils.tableview.model.ColumnHeader
import com.lokatani.lokafreshinventory.utils.tableview.model.RowHeader

class TableViewAdapter : AbstractTableAdapter<ColumnHeader, RowHeader, Cell>() {

    companion object {
        private val LOG_TAG = TableViewAdapter::class.java.simpleName
    }

    // Single Cell ViewHolder for all text-based cells
    class CellViewHolder(itemView: View) : AbstractViewHolder(itemView) {
        val cellContainer: LinearLayout = itemView.findViewById(R.id.cell_container)
        val cellTextView: TextView = itemView.findViewById(R.id.cell_data)

        fun setCell(cell: Cell?) {
            cellTextView.text = cell?.getData().toString()
            // It's necessary to remeasure itself for auto-sizing
            cellContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            cellTextView.requestLayout()
        }
    }

    override fun onCreateCellViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        Log.e(LOG_TAG, " onCreateCellViewHolder has been called")
        val layout = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.table_view_cell_layout,
                parent,
                false
            ) // Always inflate the text cell layout
        return CellViewHolder(layout)
    }

    override fun onBindCellViewHolder(
        holder: AbstractViewHolder,
        cellItemModel: Cell?,
        columnPosition: Int,
        rowPosition: Int
    ) {
        // No more 'when' statement, as all cells are now of type CellViewHolder
        val viewHolder = holder as CellViewHolder
        viewHolder.setCell(cellItemModel)
    }

    override fun onCreateColumnHeaderViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder {
        Log.e(LOG_TAG, " onCreateColumnHeaderViewHolder has been called")
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.table_view_column_header_layout, parent, false)

        // Pass the TableView instance for sorting functionality
        return ColumnHeaderViewHolder(layout, tableView)
    }

    override fun onBindColumnHeaderViewHolder(
        holder: AbstractViewHolder,
        columnHeaderItemModel: ColumnHeader?,
        columnPosition: Int
    ) {
        val columnHeaderViewHolder = holder as ColumnHeaderViewHolder
        columnHeaderViewHolder.setColumnHeader(columnHeaderItemModel)
    }

    class RowHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView) {
        val rowHeaderTextView: TextView = itemView.findViewById(R.id.row_header_textview)
    }

    override fun onCreateRowHeaderViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.table_view_row_header_layout, parent, false)
        return RowHeaderViewHolder(layout)
    }

    override fun onBindRowHeaderViewHolder(
        holder: AbstractViewHolder,
        rowHeaderItemModel: RowHeader?,
        position: Int
    ) {
        val rowHeader = rowHeaderItemModel as RowHeader
        val rowHeaderViewHolder = holder as RowHeaderViewHolder
        rowHeaderViewHolder.rowHeaderTextView.text = rowHeader.getData().toString()
    }

    override fun onCreateCornerView(parent: ViewGroup): View {
        val corner = LayoutInflater.from(parent.context)
            .inflate(R.layout.table_view_corner_layout, parent, false)

        // Row Header Sorting functionality
        corner.setOnClickListener {
            val sortState = tableView?.rowHeaderSortingStatus
            if (sortState != SortState.ASCENDING) {
                Log.d(LOG_TAG, "Order Ascending")
                tableView?.sortRowHeader(SortState.ASCENDING)
            } else {
                Log.d(LOG_TAG, "Order Descending")
                tableView?.sortRowHeader(SortState.DESCENDING)
            }
        }
        return corner
    }

    // All cells and headers are of the same type, so these can return 0
    override fun getColumnHeaderItemViewType(columnPosition: Int): Int = 0
    override fun getRowHeaderItemViewType(rowPosition: Int): Int = 0
    override fun getCellItemViewType(column: Int): Int = 0 // All cells are now of the default type
}