package com.lokatani.lokafreshinventory.ui.history

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.evrencoskun.tableview.TableView
import com.google.firebase.Timestamp
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.remote.firebase.ScanResult
import com.lokatani.lokafreshinventory.databinding.FragmentHistoryBinding
import com.lokatani.lokafreshinventory.utils.ViewModelFactory
import com.lokatani.lokafreshinventory.utils.generateCsvString
import com.lokatani.lokafreshinventory.utils.tableview.TableViewAdapter
import com.lokatani.lokafreshinventory.utils.tableview.TableViewListener
import com.lokatani.lokafreshinventory.utils.tableview.TableViewModel
import com.lokatani.lokafreshinventory.utils.tableview.model.Cell
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.util.Calendar

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var factory: ViewModelFactory
    private val viewModel: HistoryViewModel by activityViewModels {
        factory
    }

    private lateinit var tableView: TableView
    private lateinit var tableViewModel: TableViewModel
    private lateinit var tableViewAdapter: TableViewAdapter
    private var toBeExportedData: List<List<Cell>> = emptyList()

    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var createCsvFileLauncher: ActivityResultLauncher<Intent>
    private var csvContentToSave: String? = null
    private var suggestedCsvFileName: String? = null

    private var scanResults: List<ScanResult> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createCsvFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        csvContentToSave?.let { content ->
                            if (writeCsvToUri(uri, content)) {
                                val fileNameForNotification =
                                    getFileNameFromUri(uri) ?: suggestedCsvFileName
                                    ?: "exported_data.csv"
                                showExportCompleteNotification(uri, fileNameForNotification)
                            }
                            csvContentToSave = null
                            suggestedCsvFileName = null
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.file_saving_cancelled), Toast.LENGTH_SHORT
                    )
                        .show()
                    csvContentToSave = null
                    suggestedCsvFileName = null
                }
            }

        requestNotificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.notification_permission_granted_future_exports_will_notify),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.notification_permission_denied_please_grant_notification_permission_to_see_export_status),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        createNotificationChannel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        factory = ViewModelFactory.getInstance()

        (activity as AppCompatActivity).setSupportActionBar(binding.dataToolbar)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.history_data)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.filter_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_filter -> {
                        FilterBottomSheet().show(parentFragmentManager, FilterBottomSheet.TAG)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        checkAndRequestNotificationPermission()

        tableView = binding.tableView
        setupTableView()

        binding.apply {
            fabExport.setOnClickListener {
                exportFilteredDataToCsv()
            }
        }

        lifecycleScope.launch {
            viewModel.fetchScanResults()
        }
        observeFirestoreScanResults()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("HistoryFragment", "Requesting notification permission.")
                requestNotificationPermissionLauncher.launch(permission)
            }
        }
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
                    binding.tableView.visibility = View.GONE
                    binding.tvEmptyTable.visibility = View.GONE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    scanResults = result.data
                    viewModel.prepareFilterData(scanResults)
                    applyAllFilters()
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tableView.visibility = View.GONE
                    binding.tvEmptyTable.visibility = View.VISIBLE
                    binding.tvEmptyTable.text = result.error
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_message, result.error), Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

        viewModel.currentFilterState.observe(viewLifecycleOwner) { filterState ->
            if (scanResults.isNotEmpty()) {
                applyAllFilters()
            }
        }
    }

    private fun applyAllFilters() {
        val filterState = viewModel.currentFilterState.value ?: return

        // Get the complete list of data
        var filteredList = scanResults

        // Apply User filter
        filterState.user?.let { user ->
            filteredList = filteredList.filter { it.user == user }
        }

        // Apply Vegetable filter
        filterState.vegetable?.let { vegetable ->
            filteredList = filteredList.filter { it.vegResult == vegetable }
        }

        // Apply Weight filter
        if (filterState.minWeight != null && filterState.maxWeight != null) {
            filteredList = filteredList.filter {
                val weight = it.vegWeight
                weight >= filterState.minWeight && weight <= filterState.maxWeight
            }
        }

        // Apply Date Filter
        if (filterState.startDateMillis != null && filterState.endDateMillis != null) {
            // The start of the selected day
            val startCal =
                Calendar.getInstance().apply { timeInMillis = filterState.startDateMillis }
            startCal.set(Calendar.HOUR_OF_DAY, 0)
            startCal.set(Calendar.MINUTE, 0)
            startCal.set(Calendar.SECOND, 0)
            val startTimestamp = Timestamp(startCal.time)

            // The end of the selected day
            val endCal = Calendar.getInstance().apply { timeInMillis = filterState.endDateMillis }
            endCal.set(Calendar.HOUR_OF_DAY, 23)
            endCal.set(Calendar.MINUTE, 59)
            endCal.set(Calendar.SECOND, 59)
            val endTimestamp = Timestamp(endCal.time)

            filteredList = filteredList.filter { scanResult ->
                val scanTimestamp = scanResult.date
                scanTimestamp != null && scanTimestamp >= startTimestamp && scanTimestamp <= endTimestamp
            }
        }

        if (filteredList.isNotEmpty()) {
            tableViewModel = TableViewModel(filteredList)
            tableViewAdapter.setAllItems(
                tableViewModel.getColumnHeaderList(),
                tableViewModel.getRowHeaderList(),
                tableViewModel.getCellList()
            )

            toBeExportedData = tableViewModel.getCellList().map { row -> ArrayList(row) }

            binding.tableView.visibility = View.VISIBLE
            binding.tvEmptyTable.visibility = View.GONE
        } else {
            tableViewAdapter.setAllItems(emptyList(), emptyList(), emptyList()) // Clear the table
            binding.tableView.visibility = View.GONE
            binding.tvEmptyTable.visibility = View.VISIBLE
            toBeExportedData = emptyList()
        }
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.export_notification_channel_name)
        val descriptionText = getString(R.string.export_notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(EXPORT_NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showExportCompleteNotification(fileUri: Uri, fileName: String) {
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "text/csv")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val pendingIntentFlags =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            requireContext(),
            System.currentTimeMillis().toInt(),
            viewIntent,
            pendingIntentFlags
        )

        val builder = NotificationCompat.Builder(requireContext(), EXPORT_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(getString(R.string.export_complete_title))
            .setContentText(getString(R.string.export_complete_text, fileName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(requireContext())
                    .notify(EXPORT_NOTIFICATION_ID, builder.build())
            } else {
                Log.w(
                    "HistoryFragment",
                    "POST_NOTIFICATIONS permission not granted. Cannot show export notification."
                )
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_grant_notification_permission_to_see_export_status),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            NotificationManagerCompat.from(requireContext())
                .notify(EXPORT_NOTIFICATION_ID, builder.build())
        }
        Toast.makeText(
            requireContext(),
            getString(R.string.data_exported_check_notification), Toast.LENGTH_LONG
        )
            .show()
    }


    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            try {
                requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (displayNameIndex != -1) {
                            fileName = cursor.getString(displayNameIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HistoryFragment", "Error getting filename from URI", e)
            }
        }
        if (fileName == null) {
            fileName = uri.lastPathSegment
        }
        return fileName
    }

    private fun exportFilteredDataToCsv() {
        if (!::tableViewModel.isInitialized) {
            Toast.makeText(
                requireContext(),
                getString(R.string.data_has_not_been_loaded_yet), Toast.LENGTH_SHORT
            )
                .show()
            return
        }

        // 1. Get Column Headers
        val columnHeaders = tableViewModel.getColumnHeaderList()
        if (columnHeaders.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.no_header_data_to_export), Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 2. Use the stored Filtered Cell Data
        val filteredCellDataToExport: List<List<Cell>> = toBeExportedData

        if (filteredCellDataToExport.isEmpty()) {
            if (tableViewModel.getCellList().isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_data_available_to_export), Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_data_matches_the_current_filters_to_export),
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        // 3. Generate CSV String
        try {
            csvContentToSave = generateCsvString(columnHeaders, filteredCellDataToExport)
        } catch (e: Exception) { /* ... */ return
        }

        if (csvContentToSave.isNullOrEmpty() && filteredCellDataToExport.isNotEmpty()) { /* ... */ return
        } else if (csvContentToSave.isNullOrEmpty()) { /* ... */ return
        }

        suggestedCsvFileName = "filtered_inventory_data_${System.currentTimeMillis()}.csv"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, suggestedCsvFileName)
        }
        createCsvFileLauncher.launch(intent)
    }


    private fun writeCsvToUri(uri: Uri, content: String): Boolean {
        return try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(content)
                }
            }
            true
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.csv_export_failed) + ": ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            Log.e("HistoryFragment", "Error writing CSV to URI", e)
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        csvContentToSave = null
    }

    companion object {
        const val EXPORT_NOTIFICATION_CHANNEL_ID = "export_file_channel"
        const val EXPORT_NOTIFICATION_ID = 101
    }
}