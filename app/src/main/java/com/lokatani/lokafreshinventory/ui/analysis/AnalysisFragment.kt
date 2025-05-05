package com.lokatani.lokafreshinventory.ui.analysis

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.MaterialDatePicker
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

    override fun onResume() {
        super.onResume()

        binding.apply {
            btnPredictDate.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Prediction Date")
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
                            Log.d("PREDICTION", "Loading")
                        }

                        is Result.Success<*> -> {
                            Log.d("PREDICTION", "Success")
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
                                Log.d(TAG, "Error")
                                Toast.makeText(
                                    requireContext(),
                                    "Error Analysing Data",
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