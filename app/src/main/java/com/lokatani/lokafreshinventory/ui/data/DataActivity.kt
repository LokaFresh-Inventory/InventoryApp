package com.lokatani.lokafreshinventory.ui.data

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.evrencoskun.tableview.TableView
import com.lokatani.lokafreshinventory.BuildConfig
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.databinding.ActivityDataBinding
import com.lokatani.lokafreshinventory.ui.history.HistoryViewModel
import com.lokatani.lokafreshinventory.utils.ViewModelFactory
import com.lokatani.lokafreshinventory.utils.download.AndroidDownloader
import com.lokatani.lokafreshinventory.utils.showToast
import com.lokatani.lokafreshinventory.utils.tableview.TableViewAdapter
import com.lokatani.lokafreshinventory.utils.tableview.TableViewListener
import com.lokatani.lokafreshinventory.utils.tableview.TableViewModel

class DataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataBinding
    private lateinit var downloader: AndroidDownloader

    private lateinit var factory: ViewModelFactory
    private val viewModel: HistoryViewModel by viewModels {
        factory
    }

    // Declare TableView and its adapter
    private lateinit var tableView: TableView
    private lateinit var tableViewAdapter: TableViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        factory = ViewModelFactory.getInstance(this)

        setSupportActionBar(binding.dataToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Data"

        downloader = AndroidDownloader(this)

        tableView = binding.tableview
        setupTableView()

        observeScanResults()
    }

    private fun setupTableView() {
        tableViewAdapter = TableViewAdapter()
        tableView.setAdapter(tableViewAdapter)
        tableView.tableViewListener = TableViewListener(this, tableView)
    }

    private fun observeScanResults() {
        viewModel.getAllResult().observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val scanResults = result.data
                    if (scanResults.isNotEmpty()) {
                        val tableViewModel = TableViewModel(scanResults)

                        // Set the data to the adapter
                        tableViewAdapter.setAllItems(
                            tableViewModel.getColumnHeaderList(),
                            tableViewModel.getRowHeaderList(),
                            tableViewModel.getCellList()
                        )
                        tableView.invalidate()
                    } else {
                        showToast("No scan results to display.")
                        tableView.visibility = View.GONE
                        binding.tvEmptyTable.visibility = View.VISIBLE
                    }
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showToast("Error loading data: ${result.error}")
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()

        binding.apply {
            fabExport.setOnClickListener {
                downloader.downloadFile(BuildConfig.EXPORT_DATA_API)
                showToast("Downloading")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}