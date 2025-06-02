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
            edEmail.addTextChangedListener { editable ->
                val email = editable.toString()
                if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tilEmail.isErrorEnabled = true
                    tilEmail.error = "Invalid Email Format" // Use string resource
                } else {
                    tilEmail.isErrorEnabled = false
                    tilEmail.error = null // Clear error
                }
            }

            btnLogin.setOnClickListener {
                val email = edEmail.text.toString().trim()
                val password = edPassword.text.toString() // Passwords can have spaces

                tilEmail.error = null
                tilEmail.isErrorEnabled = false

                var isValid = true
                if (email.isEmpty()) {
                    tilEmail.error = "Please fill your email" // Use string resource
                    tilEmail.isErrorEnabled = true
                    isValid = false
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tilEmail.error = ""
                    tilEmail.isErrorEnabled = true
                    isValid = false
                }

                if (!isValid) {
                    if (email.isEmpty() && password.isEmpty()) {
                        showToast("Please fill all fields")
                        if (tilEmail.editText != null) tilEmail.editText!!.requestFocus()
                    } else if (email.isEmpty() && tilEmail.editText != null) {
                        tilEmail.editText!!.requestFocus()
                    } else if (password.isEmpty() && tilPassword.editText != null) {
                        tilPassword.editText!!.requestFocus()
                    }
                    return@setOnClickListener
                }

                showLoading(true)
                signIn(email, password)
            }

            btnRegister.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressOverlayContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this@LoginActivity) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Log.d(TAG, "SignIn: Successful")
                    showToast("Login Successful")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.e(TAG, "SignIn: Failure", task.exception)
                    val errorMessage = task.exception?.message ?: "Authentication Failed"
                    showToast(errorMessage)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    companion object {
        private const val TAG = "LOGIN"
    }
}