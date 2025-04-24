package com.lokatani.lokafreshinventory.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lokatani.lokafreshinventory.MainActivity
import com.lokatani.lokafreshinventory.databinding.ActivityLoginBinding
import com.lokatani.lokafreshinventory.ui.register.RegisterActivity
import com.lokatani.lokafreshinventory.utils.showToast

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

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
                val email = edEmail.text.toString()
                val password = edPassword.text.toString()

                if (email.isEmpty() && password.isNotEmpty()) {
                    showToast("Please fill your Email")
                } else if (email.isNotEmpty() && password.isEmpty()) {
                    showToast("Please insert your password")
                } else if (email.isEmpty() && password.isEmpty()) {
                    showToast("Please fill all fields")
                } else {
                    progressBar.visibility = View.VISIBLE
                    signIn(email, password)
                }
            }

            btnRegister.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this@LoginActivity) { task ->
                if (task.isSuccessful) {
                    binding.progressBar.visibility = View.GONE
                    Log.d(TAG, "SignIn: Successful")
                    showToast("Login Success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    binding.progressBar.visibility = View.GONE
                    Log.e(TAG, "SignIn: Failure", task.exception)
                    showToast("Authentication Failed")
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val loginIntent = Intent(this@LoginActivity, MainActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        private const val TAG = "LOGIN"
    }
}
