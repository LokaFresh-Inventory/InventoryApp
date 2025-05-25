package com.lokatani.lokafreshinventory.ui.history

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.local.entity.ScanResult
import com.lokatani.lokafreshinventory.databinding.ActivityHistoryBinding
import com.lokatani.lokafreshinventory.utils.ViewModelFactory
import com.lokatani.lokafreshinventory.utils.showToast

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding

    private lateinit var factory: ViewModelFactory
    private val historyViewModel: HistoryViewModel by viewModels {
        factory
    }

    private val historyAdapter = HistoryAdapter { result ->
        historyViewModel.deleteScanResult(result)
    }
    private lateinit var gridLayoutManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        factory =
            ViewModelFactory.getInstance(this) // Ensure this correctly provides your repository
        gridLayoutManager = GridLayoutManager(this, getSpanCount())

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (historyAdapter.getItemViewType(position)) {
                    HistoryAdapter.TYPE_HEADER -> gridLayoutManager.spanCount // Full span
                    HistoryAdapter.TYPE_CONTENT -> 1 // Default span per item
                    else -> 1
                }
            }
        }

        setSupportActionBar(binding.historyToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Input History"

        // Set up the RecyclerView
        binding.rvResultList.apply {
            layoutManager = gridLayoutManager
            setHasFixedSize(true)
            adapter = historyAdapter
        }

        binding.btnFirestore.setOnClickListener {
            // Show a temporary message indicating sync started
            showToast("Starting data backup to Firestore...")

            historyViewModel.syncDataToFirestore()

            historyViewModel.syncStatus.observe(this) { result ->
                if (result != null) {
                    when (result) {
                        is Result.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is Result.Success -> {
                            binding.progressBar.visibility = View.GONE
                            showToast("Data Backup Success")
                        }

                        is Result.Error -> {
                            binding.progressBar.visibility = View.GONE
                            showToast("Data Backup Failed")
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        historyViewModel.getAllResult().observe(this) { result ->
            if (result != null) {
                when (result) {
                    is Result.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvResultList.visibility = View.GONE
                        binding.tvEmpty.visibility = View.GONE
                    }

                    is Result.Success -> {
                        binding.progressBar.visibility = View.GONE
                        val resultData = result.data
                        if (resultData.isEmpty()) {
                            binding.tvEmpty.visibility = View.VISIBLE
                            binding.rvResultList.visibility = View.GONE
                        } else {
                            binding.tvEmpty.visibility = View.GONE
                            binding.rvResultList.visibility = View.VISIBLE
                            val listWithHeader = prepareListWithHeaders(resultData)
                            historyAdapter.submitList(listWithHeader)
                        }
                    }

                    is Result.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvResultList.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                        Snackbar.make(
                            binding.root,
                            getString(R.string.error_occured) + result.error,
                            Snackbar.LENGTH_SHORT
                        ).setAction("Dismiss") {
                        }.show()
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed() // Handle back button
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getSpanCount(): Int {
        return if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            4
        } else {
            2
        }
    }

    sealed class ListItem {
        data class Header(val date: String) : ListItem()
        data class Content(val result: ScanResult) : ListItem()
    }

    private fun prepareListWithHeaders(results: List<ScanResult>): List<ListItem> {
        // Sort results by date then by ID (or another stable order) to ensure consistent grouping
        val sortedResults =
            results.sortedWith(compareByDescending<ScanResult> { it.date }.thenBy { it.id })
        val groupedResults = sortedResults.groupBy { it.date }
        val sortedDates =
            groupedResults.keys.sortedDescending() // Ensure dates are sorted correctly

        val listItems = mutableListOf<ListItem>()
        sortedDates.forEach { date ->
            listItems.add(ListItem.Header(date))
            listItems.addAll(groupedResults[date]!!.map { ListItem.Content(it) })
        }

        return listItems
    }
}