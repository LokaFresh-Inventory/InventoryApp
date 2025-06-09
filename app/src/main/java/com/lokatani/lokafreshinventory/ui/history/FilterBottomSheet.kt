package com.lokatani.lokafreshinventory.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.slider.RangeSlider
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.databinding.TableFilterSheetBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: TableFilterSheetBinding? = null
    private val binding get() = _binding!!

    private val historyViewModel: HistoryViewModel by activityViewModels()

    private var minWeightFilter: Float? = null
    private var maxWeightFilter: Float? = null
    private var startDate: Long? = null
    private var endDate: Long? = null
    private val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TableFilterSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinnersWithData()
        setupInstantFilterListeners()
        setupRangeSlider()
        setupDatePicker()
        setupResetButton()
    }

    private fun setupSpinnersWithData() {
        historyViewModel.userListForFilter.observe(viewLifecycleOwner) { users ->
            val userAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, users)
            binding.actvUser.setAdapter(userAdapter)
            val currentUserFilter = historyViewModel.currentFilterState.value?.user
            binding.actvUser.setText(currentUserFilter ?: "All", false)
        }

        historyViewModel.vegetableListForFilter.observe(viewLifecycleOwner) { vegetables ->
            val vegAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                vegetables
            )
            binding.actvVegetable.setAdapter(vegAdapter)
            val currentVegFilter = historyViewModel.currentFilterState.value?.vegetable
            binding.actvVegetable.setText(currentVegFilter ?: "All", false)
        }

        val currentMin =
            historyViewModel.currentFilterState.value?.minWeight ?: binding.sliderWeight.valueFrom
        val currentMax =
            historyViewModel.currentFilterState.value?.maxWeight ?: binding.sliderWeight.valueTo
        binding.sliderWeight.values = listOf(currentMin, currentMax)
        binding.tvWeightRange.text =
            getString(R.string.weight_range_value, currentMin.toInt(), currentMax.toInt())
    }

    private fun setupInstantFilterListeners() {
        val itemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            triggerFilterUpdate()
        }

        binding.actvUser.onItemClickListener = itemClickListener
        binding.actvVegetable.onItemClickListener = itemClickListener
    }

    private fun setupRangeSlider() {
        binding.sliderWeight.addOnChangeListener { slider, value, fromUser ->
            // Update the label TextView as the slider moves
            val values = slider.values
            binding.tvWeightRange.text =
                getString(R.string.weight_range_value, values[0].toInt(), values[1].toInt())
        }

        binding.sliderWeight.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {
                // No action
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                triggerFilterUpdate()
            }
        })
    }


    private fun triggerFilterUpdate() {
        val selectedUser = binding.actvUser.text.toString()
        val selectedVegetable = binding.actvVegetable.text.toString()
        val weightValues = binding.sliderWeight.values
        minWeightFilter = weightValues[0]
        maxWeightFilter = weightValues[1]
        val allText = "All"

        historyViewModel.applyFilters(
            selectedUser,
            selectedVegetable,
            minWeightFilter,
            maxWeightFilter,
            startDate,
            endDate,
            allText
        )
    }

    private fun setupDatePicker() {
        val initialStartDate = historyViewModel.currentFilterState.value?.startDateMillis
        val initialEndDate = historyViewModel.currentFilterState.value?.endDateMillis
        startDate = initialStartDate
        endDate = initialEndDate
        updateDateRangeText()

        binding.etDate.setOnClickListener {
            val datePickerBuilder = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")

            if (startDate != null && endDate != null) {
                datePickerBuilder.setSelection(androidx.core.util.Pair(startDate, endDate))
            }

            val datePicker = datePickerBuilder.build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                startDate = selection.first
                endDate = selection.second
                updateDateRangeText()
                triggerFilterUpdate()
            }

            datePicker.show(childFragmentManager, "DATE_PICKER")
        }
    }

    private fun updateDateRangeText() {
        if (startDate != null && endDate != null) {
            binding.etDate.setText(
                getString(
                    R.string.range_item_value,
                    simpleDateFormat.format(Date(startDate!!)),
                    simpleDateFormat.format(
                        Date(
                            endDate!!
                        )
                    )
                )
            )
        } else {
            binding.etDate.setText("")
        }
    }

    private fun setupResetButton() {
        val allText = "All"
        binding.apply {
            btnResetUser.setOnClickListener {
                val selectedVegetable = binding.actvVegetable.text.toString()
                historyViewModel.clearUserFilters(
                    selectedVegetable,
                    allText,
                    minWeightFilter,
                    maxWeightFilter
                )
                actvUser.setText(allText, false)
            }

            btnResetVeg.setOnClickListener {
                val selectedUser = binding.actvUser.text.toString()
                historyViewModel.clearVegetableFilters(
                    selectedUser,
                    allText,
                    minWeightFilter,
                    maxWeightFilter
                )
                actvVegetable.setText(allText, false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}