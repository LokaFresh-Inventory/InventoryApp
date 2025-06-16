package com.lokatani.lokafreshinventory.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.remote.firebase.ScanResult
import com.lokatani.lokafreshinventory.databinding.FragmentHomeBinding
import com.lokatani.lokafreshinventory.ui.chatbot.ChatbotActivity
import com.lokatani.lokafreshinventory.ui.scan.ScanActivity
import com.lokatani.lokafreshinventory.utils.ViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var factory: ViewModelFactory
    private val historyViewModel: HomeViewModel by viewModels {
        factory
    }

    // Define a list of colors to use for different lines
    private val chartColors = arrayOf(
        Color.RED,
        Color.GREEN
    )
    private var colorIndex = 0 // Start to cycle through list of colors

    private val numberFormatter = NumberFormat.getNumberInstance(Locale("in", "ID"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        factory = ViewModelFactory.getInstance()

        binding.apply {
            layoutItemKale.tvItemName.text = getString(R.string.kale)
            layoutItemBayamMerah.tvItemName.text = getString(R.string.bayam_merah)
            layoutItemLastInput.tvItemName.text = getString(R.string.last_input)
            layoutItemKale.ivItemImage.setImageResource(R.drawable.kale_landscape)
            layoutItemBayamMerah.ivItemImage.setImageResource(R.drawable.bayam_merah_landscape)
            layoutItemLastInput.ivItemImage.setImageResource(R.drawable.stopwatch)

            btnChatbot.setOnClickListener {
                startActivity(Intent(requireContext(), ChatbotActivity::class.java))
            }
            btnScan.setOnClickListener {
                startActivity(Intent(requireContext(), ScanActivity::class.java))
            }
        }

        historyViewModel.fetchDataForHomeScreen()

        historyViewModel.homeUiState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.chartProgressBar.visibility = View.VISIBLE
                    binding.monthlyLineChart.visibility = View.GONE
                    binding.tvNoChartData.visibility = View.GONE
                }

                is Result.Success -> {
                    binding.chartProgressBar.visibility = View.GONE
                    val data = result.data
                    updateSummaryCards(data.totalWeights, data.lastInput)
                    if (data.monthlyChartData.isNotEmpty()) {
                        binding.monthlyLineChart.visibility = View.VISIBLE
                        binding.tvNoChartData.visibility = View.GONE
                        setupLineChart(data.monthlyChartData)
                    } else {
                        binding.monthlyLineChart.visibility = View.GONE
                        binding.tvNoChartData.visibility = View.VISIBLE
                    }
                }

                is Result.Error -> {
                    binding.chartProgressBar.visibility = View.GONE
                    binding.monthlyLineChart.visibility = View.GONE
                    binding.tvNoChartData.visibility = View.VISIBLE
                    Toast.makeText(
                        activity,
                        getString(R.string.error_loading_chart_data, result.error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateSummaryCards(totalWeights: Map<String, Int>, lastInput: ScanResult?) {
        // Update Kale Card
        val kaleTotalWeight = numberFormatter.format(totalWeights[getString(R.string.kale)] ?: 0)
        binding.layoutItemKale.tvItemValue.text = getString(R.string.gram, kaleTotalWeight)

        // Update Bayam Merah Card
        val bayamMerahTotalWeight =
            numberFormatter.format(totalWeights[getString(R.string.bayam_merah)] ?: 0)
        binding.layoutItemBayamMerah.tvItemValue.text =
            getString(R.string.gram, bayamMerahTotalWeight)

        // Update Last Input Card
        if (lastInput != null) {
            // Format the Timestamp for display
            val displayDate = lastInput.date?.let { timestamp ->
                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(timestamp.toDate())
            } ?: getString(R.string.unknown_date)
            val lastInputVegetable = lastInput.vegResult
            val lastInputWeight = numberFormatter.format(lastInput.vegWeight)
            val lastInputText = getString(
                R.string.last_input_data,
                lastInputVegetable,
                lastInputWeight,
                displayDate
            )
            binding.layoutItemLastInput.tvItemValue.text = lastInputText
        } else {
            binding.layoutItemLastInput.tvItemValue.text = getString(R.string.no_recent_input)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupLineChart(data: Map<String, Map<String, Int>>) {
        val lineChart: LineChart = binding.monthlyLineChart

        val months = data.keys.sorted()
        val xLabels = ArrayList<String>()

        // Prepare x-axis labels
        for (month in months) {
            val yearMonthDisplay = try {
                val ym = YearMonth.parse(month)
                ym.format(
                    DateTimeFormatter.ofPattern(
                        "MMM yyyy",
                        Locale.getDefault()
                    )
                ) // e.g., "Jan 2025"
            } catch (e: DateTimeParseException) {
                month // Fallback
            }
            xLabels.add(yearMonthDisplay)
        }

        // Get all unique vegetable types present in the data
        val distinctVegTypes = data.values.flatMap { it.keys }.distinct().sorted()

        val dataSets = ArrayList<ILineDataSet>()
        colorIndex = 0 // Reset color index for each chart setup

        // Iterate through each distinct vegetable type to create a LineDataSet
        for (vegType in distinctVegTypes) {
            val entries = ArrayList<Entry>()
            for ((index, month) in months.withIndex()) {
                val monthlyData = data[month]
                val vegWeight = monthlyData?.get(vegType) ?: 0 // Get weight for current vegType
                entries.add(Entry(index.toFloat(), vegWeight.toFloat()))
            }

            val currentDataSet = LineDataSet(entries, getString(R.string.veg_weight, vegType))
            currentDataSet.color = getNextChartColor() // Assign a color
            currentDataSet.valueTextColor = Color.BLACK
            currentDataSet.setDrawCircles(true)
            currentDataSet.setCircleColor(currentDataSet.color) // Circle color matches line color
            currentDataSet.lineWidth = 2f
            dataSets.add(currentDataSet)
        }

        val lineData = LineData(dataSets)
        lineChart.data = lineData

        // Customize X-axis (months)
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
        lineChart.xAxis.granularity = 1f
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.xAxis.setLabelCount(xLabels.size, false)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.textColor = Color.BLACK
        lineChart.xAxis.textSize = 10f
        lineChart.xAxis.labelRotationAngle = -45f // Rotate labels if they overlap

        // Customize Y-axis (weight)
        lineChart.axisLeft.axisMinimum = 0f
        lineChart.axisLeft.textColor = Color.BLACK
        lineChart.axisLeft.textSize = 10f
        lineChart.axisRight.isEnabled = false

        lineChart.legend.isEnabled = false

        // Other chart customizations
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.setDrawGridBackground(false)
        lineChart.animateX(1500)

        lineChart.invalidate() // Refresh the chart
    }

    // Helper function to get a unique color for each dataset
    private fun getNextChartColor(): Int {
        val color = chartColors[colorIndex % chartColors.size]
        colorIndex++
        return color
    }
}