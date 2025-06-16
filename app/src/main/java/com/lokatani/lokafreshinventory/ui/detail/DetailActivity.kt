package com.lokatani.lokafreshinventory.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.databinding.ActivityDetailBinding
import com.lokatani.lokafreshinventory.ui.scan.ScanActivity
import com.lokatani.lokafreshinventory.utils.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var factory: ViewModelFactory
    private var currentUser: String? = null

    private val detailViewModel: DetailViewModel by viewModels {
        factory
    }

    private var vegResult: String? = null
    private var vegWeight: Int = 0

    private val displayDateFormatter =
        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        factory = ViewModelFactory.getInstance()

        setSupportActionBar(binding.toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.scan_result)

        vegResult = intent.getStringExtra(EXTRA_RESULT)
        vegWeight = intent.getIntExtra(EXTRA_WEIGHT, 0)

        currentUser = Firebase.auth.currentUser?.email
    }

    override fun onResume() {
        super.onResume()

        val currentTimestamp = Timestamp.now()

        binding.apply {
            tvVegType.text = vegResult ?: getString(R.string.no_data)
            tvVegWeight.text = getString(R.string.gram, vegWeight.toString())
            tvDate.text = displayDateFormatter.format(currentTimestamp.toDate())

            buttonSave.setOnClickListener {
                detailViewModel.insertResult(
                    user = currentUser ?: getString(R.string.guest),
                    vegResult = vegResult ?: getString(R.string.no_vegetable),
                    vegWeight = vegWeight,
                    date = currentTimestamp
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