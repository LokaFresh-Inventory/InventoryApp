package com.lokatani.lokafreshinventory.ui.history

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.local.entity.ScanResult
import com.lokatani.lokafreshinventory.databinding.FragmentHistoryLocalBinding
import com.lokatani.lokafreshinventory.utils.ViewModelFactory


class HistoryLocalFragment : Fragment() {

    private var _binding: FragmentHistoryLocalBinding? = null
    private val binding get() = _binding!!

    private lateinit var factory: ViewModelFactory
    private val historyViewModel: HistoryViewModel by viewModels {
        factory
    }

    private val historyAdapter = HistoryAdapter { result ->
        historyViewModel.deleteScanResult(result)
    }
    private lateinit var gridLayoutManager: GridLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryLocalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        factory =
            ViewModelFactory.getInstance(requireContext()) // Ensure this correctly provides your repository
        gridLayoutManager = GridLayoutManager(requireContext(), getSpanCount())

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (historyAdapter.getItemViewType(position)) {
                    HistoryAdapter.TYPE_HEADER -> gridLayoutManager.spanCount // Full span
                    HistoryAdapter.TYPE_CONTENT -> 1 // Default span per item
                    else -> 1
                }
            }
        }

        binding.rvResultList.apply {
            layoutManager = gridLayoutManager
            setHasFixedSize(true)
            adapter = historyAdapter
        }

        binding.btnFirestore.setOnClickListener {
            // Show a temporary message indicating sync started
            Toast.makeText(
                requireContext(),
                "Starting data backup to Firestore...",
                Toast.LENGTH_SHORT
            ).show()

            historyViewModel.syncDataToFirestore()

            historyViewModel.syncStatus.observe(viewLifecycleOwner) { result ->
                if (result != null) {
                    when (result) {
                        is Result.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is Result.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Data Backup Success",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is Result.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Data Backup Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        observeFetchLocalData()
    }

    private fun observeFetchLocalData() {
        historyViewModel.getAllResult().observe(viewLifecycleOwner) { result ->
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