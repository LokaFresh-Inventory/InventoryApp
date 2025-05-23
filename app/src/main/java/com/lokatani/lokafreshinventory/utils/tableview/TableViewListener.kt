package com.lokatani.lokafreshinventory.utils.tableview

import android.content.Context
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.listener.ITableViewListener
import com.lokatani.lokafreshinventory.utils.tableview.holder.ColumnHeaderViewHolder

class TableViewListener(
    private val mContext: Context,
    private val mTableView: TableView
) : ITableViewListener {

    /**
     * Called when the user clicks any cell item.
     */
    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showToast("Cell $column $row has been clicked.")
    }

    /**
     * Called when the user double-clicks any cell item.
     */
    override fun onCellDoubleClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showToast("Cell $column $row has been double clicked.")
    }

    /**
     * Called when the user long-presses any cell item.
     */
    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showToast("Cell $column $row has been long pressed.")
    }

    /**
     * Called when the user clicks any column header item.
     */
    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {
        showToast("Column header $column has been clicked.")
    }

    /**
     * Called when the user double-clicks any column header item.
     */
    override fun onColumnHeaderDoubleClicked(
        columnHeaderView: RecyclerView.ViewHolder,
        column: Int
    ) {
        showToast("Column header $column has been double clicked.")
    }

    /**
     * Called when the user long-presses any column header item.
     */
    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {
        // If you had a custom long press popup for column headers
        if (columnHeaderView is ColumnHeaderViewHolder) {
            // You would create and show your popup here, similar to the library's example:
            // ColumnHeaderLongPressPopup(columnHeaderView, mTableView).show()
        }
        showToast("Column header $column has been long pressed.")
    }

    /**
     * Called when the user clicks any Row Header item.
     */
    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {
        showToast("Row header $row has been clicked.")
    }

    /**
     * Called when the user double-clicks any Row Header item.
     */
    override fun onRowHeaderDoubleClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {
        showToast("Row header $row has been double clicked.")
    }

    /**
     * Called when the user long-presses any row header item.
     */
    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {
        showToast("Row header $row has been long pressed.")
    }

    private fun showToast(message: String) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }
}