package com.lokatani.lokafreshinventory.ui.data

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.lokatani.lokafreshinventory.BuildConfig
import com.lokatani.lokafreshinventory.databinding.ActivityDataBinding
import com.lokatani.lokafreshinventory.utils.download.AndroidDownloader
import com.lokatani.lokafreshinventory.utils.showToast

class DataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataBinding
    private lateinit var downloader: AndroidDownloader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.dataToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Data"

        downloader = AndroidDownloader(this)

    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            fabExport.setOnClickListener {
                downloader.downloadFile(BuildConfig.EXPORT_DATA_API)
                showToast("Downloading")
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
}