package com.lokatani.lokafreshinventory.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.databinding.ActivityDetailBinding
import com.lokatani.lokafreshinventory.ui.scan.ScanActivity
import com.lokatani.lokafreshinventory.utils.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var factory: ViewModelFactory

    private val detailViewModel: DetailViewModel by viewModels {
        factory
    }

    private var vegResult: String? = null
    private var vegWeight: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        factory = ViewModelFactory.getInstance(this)

        setSupportActionBar(binding.toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.scan_result)

        vegResult = intent.getStringExtra(EXTRA_RESULT)
        vegWeight = intent.getStringExtra(EXTRA_WEIGHT)

        if (vegWeight == null) {
            vegWeight = "0"
        }

        if (vegResult == null) {
            vegResult = "No Item Detected"
        }
    }

    override fun onResume() {
        super.onResume()

        val currentDate = getCurrentDate()

        binding.apply {
            tvVegType.text = vegResult
            tvVegWeight.text = getString(R.string.gram, vegWeight)
            tvDate.text = currentDate

            buttonSave.setOnClickListener {
                detailViewModel.insertResult(
                    user = "Dummy User",
                    vegResult = vegResult.toString(),
                    vegWeight = vegWeight.toString().toFloat(),
                    date = currentDate
                )
                val intent = Intent(this@DetailActivity, ScanActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }

        detailViewModel.insertCompleted.observe(this) { completed ->
            if (completed == true) {
                detailViewModel.resetInsertStatus()
            }
        }

    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
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

    companion object {
        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_WEIGHT = "extra_weight"
    }
}