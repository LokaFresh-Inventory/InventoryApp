package com.lokatani.lokafreshinventory.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.RangeSlider
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.databinding.TableFilterSheetBinding

class FilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: TableFilterSheetBinding? = null
    private val binding get() = _binding!!

    private val historyViewModel: HistoryViewModel by activityViewModels()

    private var minWeightFilter: Float? = null
    private var maxWeightFilter: Float? = null


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
        // Create a single listener instance to be shared by both spinners.
        val itemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            // When any spinner item is clicked, trigger an update.
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

        // Add a listener to apply the filter only when the user finishes sliding
        binding.sliderWeight.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {
                // No action
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                // Trigger the filter update when the user lifts their finger
                triggerFilterUpdate()
            }
        })
    }


    // Modify this to include weight values
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
            allText
        )
    }

    private fun setupResetButton() {
        val allText = "All" // Use the same default text
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