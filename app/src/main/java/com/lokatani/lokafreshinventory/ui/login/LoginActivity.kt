package com.lokatani.lokafreshinventory.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lokatani.lokafreshinventory.databinding.ActivityLoginBinding
import android.util.Patterns
import androidx.core.widget.addTextChangedListener
import com.lokatani.lokafreshinventory.MainActivity
import com.lokatani.lokafreshinventory.ui.register.RegisterActivity
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

            btnLogin.setOnClickListener {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
            }

            btnRegister.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

        }
    }
}
