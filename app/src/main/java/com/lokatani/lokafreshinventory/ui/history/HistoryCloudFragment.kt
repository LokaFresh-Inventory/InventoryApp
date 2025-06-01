package com.lokatani.lokafreshinventory.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.evrencoskun.tableview.TableView
import com.lokatani.lokafreshinventory.BuildConfig
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.databinding.FragmentHistoryCloudBinding
import com.lokatani.lokafreshinventory.utils.ViewModelFactory
import com.lokatani.lokafreshinventory.utils.download.AndroidDownloader
import com.lokatani.lokafreshinventory.utils.tableview.TableViewAdapter
import com.lokatani.lokafreshinventory.utils.tableview.TableViewListener
import com.lokatani.lokafreshinventory.utils.tableview.TableViewModel
import kotlinx.coroutines.launch

class HistoryCloudFragment : Fragment() {

    private var _binding: FragmentHistoryCloudBinding? = null
    private val binding get() = _binding!!

    private lateinit var factory: ViewModelFactory
    private val viewModel: HistoryViewModel by viewModels {
        factory
    }

    private lateinit var tableView: TableView
    private lateinit var tableViewAdapter: TableViewAdapter
    private lateinit var downloader: AndroidDownloader

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryCloudBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        factory = ViewModelFactory.getInstance(requireContext())

        (activity as AppCompatActivity).setSupportActionBar(binding.dataToolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "History Data"

        downloader = AndroidDownloader(requireContext())

        tableView = binding.tableview
        setupTableView()

        binding.apply {
            fabExport.setOnClickListener {
                downloader.downloadFile(BuildConfig.EXPORT_DATA_API)
                Toast.makeText(requireContext(), "Downloading", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch and observe Firestore data
        lifecycleScope.launch {
            viewModel.fetchScanResults()
        }
        observeFirestoreScanResults()
    }

    private fun setupTableView() {
        tableViewAdapter = TableViewAdapter()
        tableView.setAdapter(tableViewAdapter)
        tableView.tableViewListener = TableViewListener(requireContext(), tableView)
    }

    private fun observeFirestoreScanResults() {
        viewModel.scanResults.observe(viewLifecycleOwner) { result ->
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
                        Toast.makeText(
                            requireContext(),
                            "No scan results to display",
                            Toast.LENGTH_SHORT
                        ).show()
                        tableView.visibility = View.GONE
                        binding.tvEmptyTable.visibility = View.VISIBLE
                    }
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Error loading data: ${result.error}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}