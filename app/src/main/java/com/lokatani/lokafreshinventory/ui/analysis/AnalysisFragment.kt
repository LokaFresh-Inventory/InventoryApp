package com.lokatani.lokafreshinventory.ui.analysis

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.data.remote.response.PredictResponse
import com.lokatani.lokafreshinventory.databinding.FragmentAnalysisBinding
import com.lokatani.lokafreshinventory.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnalysisFragment : Fragment() {

    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!

    private val factory = PredictViewModelFactory.getInstance()
    private val analysisViewModel: AnalysisViewModel by viewModels {
        factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.myToolbar)
        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.prediction_analysis)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.help_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_help -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setView(R.layout.analysis_help)
                            .show()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            btnPredictDate.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.select_prediction_date))
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()
                datePicker.show(childFragmentManager, "DATE_PICKER")

                datePicker.addOnPositiveButtonClickListener { selection ->
                    val dateDashFormatter = SimpleDateFormat("dd-MM-yyy", Locale.getDefault())
                    val formattedDate = dateDashFormatter.format(Date(selection))
                    val selectedDate = Date(selection) // Convert Long to Date
                    btnPredictDate.text = DateUtils.formatDate(
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate)
                    )

                    analysisViewModel.predict(formattedDate)
                }
            }

            analysisViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            analysisViewModel.predictionResult.observe(viewLifecycleOwner) { result ->
                if (result != null) {
                    when (result) {
                        is Result.Loading -> {
                            Log.d("PREDICTION", getString(R.string.loading))
                        }

                        is Result.Success<*> -> {
                            Log.d("PREDICTION", getString(R.string.success))
                            val predictionResult = result.data as PredictResponse
                            val predictionFirst = predictionResult.kale ?: 0
                            val predictionSecond = predictionResult.bayamMerah ?: 0
                            val predictionSum = predictionFirst + predictionSecond
                            tvResultFirst.text =
                                getString(R.string.prediction_kale, predictionFirst)
                            tvResultSecond.text =
                                getString(R.string.prediction_bayam_merah, predictionSecond)
                            tvResultTotal.text = getString(R.string.kg, predictionSum)
                        }

                        is Result.Error -> {
                            view?.let {
                                Log.d(TAG, getString(R.string.error))
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_analysing_data),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ANALYSIS"
    }
}