package com.lokatani.lokafreshinventory.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lokatani.lokafreshinventory.MainActivity
import com.lokatani.lokafreshinventory.databinding.ActivityRegisterBinding
import com.lokatani.lokafreshinventory.ui.login.LoginActivity
import com.lokatani.lokafreshinventory.utils.showToast

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            btnLogin.setOnClickListener {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }

            btnRegister.setOnClickListener {
                val name = edName.text.toString()
                val email = edEmail.text.toString()
                val password = edPassword.text.toString()

                if (name.isEmpty() || password.isEmpty()) {
                    showToast("Please fill all fields")
                } else {
                    showLoading(true)
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this@RegisterActivity) { task ->
                            showLoading(false)
                            if (task.isSuccessful) {
                                Log.d(TAG, "Register: Successful")
                                showToast("Register Success")
                                val user = auth.currentUser
                                updateUI(user)
                            } else {
                                Log.e(TAG, "Register: Failure", task.exception)
                                val errorMessage =
                                    task.exception?.message ?: "Authentication Failed"
                                showToast(errorMessage)
                            }
                        }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressOverlayContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val loginIntent = Intent(this@RegisterActivity, MainActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
        }
    }

    companion object {
        const val TAG = "REGISTER"
    }
}