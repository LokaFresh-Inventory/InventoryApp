package com.lokatani.lokafreshinventory.utils.tableview.holder

import android.view.View
import android.widget.TextView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.lokatani.lokafreshinventory.R

class RowHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView) {
    val rowHeaderTextView: TextView = itemView.findViewById(R.id.row_header_textview)
}