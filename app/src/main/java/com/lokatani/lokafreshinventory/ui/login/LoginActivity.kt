package com.lokatani.lokafreshinventory.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.databinding.ActivityLoginBinding
import android.util.Patterns
import androidx.core.widget.addTextChangedListener

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.apply {
            edEmail.addTextChangedListener {
                var email = edEmail.text.toString()
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        tilEmail.isErrorEnabled = true
                        tilEmail.error = "Email invalid"
                    } else {
                        tilEmail.error = null
                        tilEmail.isErrorEnabled = false
                    }
            }

        }
    }
}
