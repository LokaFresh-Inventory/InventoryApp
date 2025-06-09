package com.lokatani.lokafreshinventory.utils.tableview.holder

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.evrencoskun.tableview.ITableView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractSorterViewHolder
import com.evrencoskun.tableview.sort.SortState
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.utils.tableview.model.ColumnHeader

class ColumnHeaderViewHolder(
    itemView: View,
    private val tableView: ITableView?
) : AbstractSorterViewHolder(itemView) {

    companion object {
        private val LOG_TAG = ColumnHeaderViewHolder::class.java.simpleName
    }

    val columnHeaderContainer: LinearLayout = itemView.findViewById(R.id.column_header_container)
    val columnHeaderTextview: TextView = itemView.findViewById(R.id.column_header_textView)
    val columnHeaderSortButton: ImageButton = itemView.findViewById(R.id.column_header_sortButton)

    // Using lazy initialization for the click listener
    private val mSortButtonClickListener: View.OnClickListener by lazy {
        View.OnClickListener {
            when (sortState) {
                SortState.ASCENDING -> tableView?.sortColumn(adapterPosition, SortState.DESCENDING)
                SortState.DESCENDING -> tableView?.sortColumn(adapterPosition, SortState.ASCENDING)
                else -> {
                    tableView?.sortColumn(adapterPosition, SortState.DESCENDING)
                }
            }
        }
    }

    init {
        columnHeaderSortButton.setOnClickListener(mSortButtonClickListener)
    }

    /**
     * This method is calling from onBindColumnHeaderHolder on TableViewAdapter
     */
    fun setColumnHeader(columnHeader: ColumnHeader?) {
        columnHeaderTextview.text = columnHeader?.getData().toString()

        // If your TableView should have auto resize for cells & columns.
        // Then you should consider the below lines. Otherwise, you can remove them.

        // It is necessary to remeasure itself.
        columnHeaderContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        columnHeaderTextview.requestLayout()
    }

    override fun onSortingStatusChanged(sortState: SortState) {
        Log.e(
            LOG_TAG,
            " + onSortingStatusChanged: x:  " + adapterPosition + ", " +
                    "old state: " + this.sortState + ", current state: " + sortState + ", " +
                    "visibility: " + columnHeaderSortButton.visibility
        )

        super.onSortingStatusChanged(sortState)

        // It is necessary to remeasure itself.
        columnHeaderContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT

        controlSortState(sortState)

        Log.e(
            LOG_TAG,
            " - onSortingStatusChanged: x:  " + adapterPosition + ", " +
                    "old state: " + this.sortState + ", current state: " + sortState + ", " +
                    "visibility: " + columnHeaderSortButton.visibility
        )

        columnHeaderTextview.requestLayout()
        columnHeaderSortButton.requestLayout()
        columnHeaderContainer.requestLayout()
        itemView.requestLayout()
    }

    private fun controlSortState(sortState: SortState) {
        if (sortState == SortState.ASCENDING) {
            columnHeaderSortButton.visibility = View.VISIBLE
            columnHeaderSortButton.setImageResource(R.drawable.baseline_arrow_drop_down_24)
        } else if (sortState == SortState.DESCENDING) {
            columnHeaderSortButton.visibility = View.VISIBLE
            columnHeaderSortButton.setImageResource(R.drawable.baseline_arrow_drop_up_24)
        } else {
            columnHeaderSortButton.visibility = View.INVISIBLE
        }
    }
}