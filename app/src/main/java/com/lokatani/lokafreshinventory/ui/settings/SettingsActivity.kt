package com.lokatani.lokafreshinventory.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.settingToolbar)
        supportActionBar?.setTitle(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}