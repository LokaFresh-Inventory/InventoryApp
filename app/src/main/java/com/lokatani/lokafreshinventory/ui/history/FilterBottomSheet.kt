package com.lokatani.lokafreshinventory.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lokatani.lokafreshinventory.databinding.TableFilterSheetBinding

class FilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: TableFilterSheetBinding? = null
    private val binding get() = _binding!!


    private val historyViewModel: HistoryViewModel by activityViewModels()


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
        setupInstantFilterListeners() // New method for listeners
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

    private fun triggerFilterUpdate() {
        // This function reads the CURRENT state of both spinners and updates the ViewModel.
        val selectedUser = binding.actvUser.text.toString()
        val selectedVegetable = binding.actvVegetable.text.toString()
        val allText = "All" // Should match the one used in the ViewModel

        historyViewModel.applyFilters(selectedUser, selectedVegetable, allText)
    }

    private fun setupResetButton() {
        val allText = "All" // Use the same default text
        binding.apply {
            btnResetUser.setOnClickListener {
                val selectedVegetable = binding.actvVegetable.text.toString()
                historyViewModel.clearUserFilters(selectedVegetable, allText)
                actvUser.setText(allText, false)
            }

            btnResetVeg.setOnClickListener {
                val selectedUser = binding.actvUser.text.toString()
                historyViewModel.clearVegetableFilters(selectedUser, allText)
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