package com.lokatani.lokafreshinventory.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.data.local.entity.ScanResult
import com.lokatani.lokafreshinventory.databinding.HeaderRowBinding
import com.lokatani.lokafreshinventory.databinding.HistoryRowWithImageBinding
import com.lokatani.lokafreshinventory.ui.history.HistoryActivity.ListItem
import com.lokatani.lokafreshinventory.utils.DateUtils

class HistoryAdapter(private val onDeleteHold: (ScanResult) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ListItem>()
    fun submitList(list: List<ListItem>) {
        items.clear()
        items.addAll(list)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding =
                    HeaderRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }

            TYPE_CONTENT -> {
                val binding = HistoryRowWithImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ContentViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder) {
            is HeaderViewHolder -> holder.bind((items[position] as ListItem.Header).date)
            is ContentViewHolder -> {
                val result = (items[position] as ListItem.Content).result
                holder.bind(result)
                holder.itemView.setOnLongClickListener {
                    MaterialAlertDialogBuilder(holder.itemView.context).apply {
                        setIcon(R.drawable.baseline_delete_outline_24)
                        setTitle("Delete History")
                        setMessage("Do you want to delete this item?")
                        setPositiveButton("Yes") { _, _ ->
                            onDeleteHold(result)
                        }
                        setNegativeButton("No", null)
                        show()
                    }
                    true
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.Content -> TYPE_CONTENT
        }
    }


    class HeaderViewHolder(private val binding: HeaderRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.tvHeaderDate.text = DateUtils.formatDate(date)
        }
    }

    class ContentViewHolder(private val binding: HistoryRowWithImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: ScanResult) {
            val vegType = result.vegResult
            val vegWeight = result.vegWeight
            binding.apply {
                tvPredictionValue.text = vegType
                tvWeightValue.text = vegWeight.toString()
                if (vegType == "Kale") {
                    ivVeg.setImageResource(R.drawable.kale)
                } else {
                    ivVeg.setImageResource(R.drawable.bayam_merah)
                }
            }
        }
    }

    companion object {
        internal const val TYPE_HEADER = 0
        internal const val TYPE_CONTENT = 1
    }
}