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
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.databinding.ActivityLoginBinding
import com.lokatani.lokafreshinventory.ui.register.RegisterActivity
import com.lokatani.lokafreshinventory.utils.showToast

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    private var isEmailValid = false
    private var isPassValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.apply {
            btnLogin.isEnabled = isEmailValid && isPassValid

            edEmail.addTextChangedListener { editable ->
                val email = editable.toString()
                if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tilEmail.isErrorEnabled = true
                    tilEmail.error = getString(R.string.invalid_email_format)
                    isEmailValid = false
                } else if (email.isEmpty()) {
                    tilEmail.isErrorEnabled = true
                    tilEmail.error = getString(R.string.please_fill_your_email)
                    isEmailValid = false
                } else {
                    tilEmail.isErrorEnabled = false
                    tilEmail.error = null
                    isEmailValid = true
                }
                btnLogin.isEnabled = isEmailValid && isPassValid
            }

            edPassword.addTextChangedListener { editable ->
                val password = editable.toString()
                if (password.isNotEmpty() && password.length < 6) {
                    tilPassword.isErrorEnabled = true
                    tilPassword.error = getString(R.string.password_must_have_at_least_6_characters)
                    isPassValid = false
                } else if (password.isEmpty()) {
                    tilPassword.isErrorEnabled = true
                    tilPassword.error = getString(R.string.please_fill_your_password)
                    isPassValid = false
                } else {
                    tilPassword.isErrorEnabled = false
                    tilPassword.error = null
                    isPassValid = true
                }
                btnLogin.isEnabled = isEmailValid && isPassValid
            }

            btnLogin.setOnClickListener {
                val email = edEmail.text.toString().trim()
                val password = edPassword.text.toString()

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
                    showToast(getString(R.string.login_successful))
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.e(TAG, "SignIn: Failure", task.exception)
                    val errorMessage =
                        task.exception?.message ?: getString(R.string.authentication_failed)
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