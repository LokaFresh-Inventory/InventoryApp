package com.lokatani.lokafreshinventory.ui.register

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lokatani.lokafreshinventory.MainActivity
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.data.remote.firebase.User
import com.lokatani.lokafreshinventory.databinding.ActivityRegisterBinding
import com.lokatani.lokafreshinventory.ui.login.LoginActivity
import com.lokatani.lokafreshinventory.utils.showToast

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var binding: ActivityRegisterBinding

    private var isNameValid = false
    private var isEmailValid = false
    private var isPassValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            btnRegister.isEnabled = isNameValid && isEmailValid && isPassValid

            edName.addTextChangedListener { editable ->
                val username = editable.toString()
                if (username.isNotEmpty() && username.length < 3) {
                    tilName.isErrorEnabled = true
                    tilName.error = getString(R.string.username_must_have_at_least_3_characters)
                    isNameValid = false
                } else if (username.isEmpty()) {
                    tilName.isErrorEnabled = true
                    tilName.error = getString(R.string.please_make_a_username)
                    isNameValid = false
                } else {
                    tilName.isErrorEnabled = false
                    tilName.error = null
                    isNameValid = true
                }
                btnRegister.isEnabled = isNameValid && isEmailValid && isPassValid
            }

            edEmail.addTextChangedListener { editable ->
                val email = editable.toString()
                if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tilEmail.isErrorEnabled = true
                    tilEmail.error = getString(R.string.invalid_email_format)
                    isEmailValid = false
                } else if (email.isEmpty()) {
                    tilEmail.isErrorEnabled = true
                    tilEmail.error = getString(R.string.fill_your_email_to_register)
                    isEmailValid = false
                } else {
                    tilEmail.isErrorEnabled = false
                    tilEmail.error = null
                    isEmailValid = true
                }
                btnRegister.isEnabled = isNameValid && isEmailValid && isPassValid
            }

            edPassword.addTextChangedListener { editable ->
                val password = editable.toString()
                if (password.isNotEmpty() && password.length < 6) {
                    tilPassword.isErrorEnabled = true
                    tilPassword.error = getString(R.string.password_must_have_at_least_6_characters)
                    isPassValid = false
                } else if (password.isEmpty()) {
                    tilPassword.isErrorEnabled = true
                    tilPassword.error = getString(R.string.make_a_strong_password)
                    isPassValid = false
                } else {
                    tilPassword.isErrorEnabled = false
                    tilPassword.error = null
                    isPassValid = true
                }
                btnRegister.isEnabled = isEmailValid && isPassValid
            }

            btnLogin.setOnClickListener {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }

            btnRegister.setOnClickListener {
                val username = edName.text.toString().trim()
                val email = edEmail.text.toString().trim()
                val password = edPassword.text.toString().trim()

                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    showToast(getString(R.string.please_fill_all_fields))
                } else {
                    showLoading(true)
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this@RegisterActivity) { task ->
                            showLoading(false)
                            if (task.isSuccessful) {
                                Log.d(TAG, "Register: Successful")
                                val user = auth.currentUser
                                val userUId = user?.uid

                                if (userUId != null) {
                                    val userToFirestore = User(
                                        uid = userUId,
                                        username = username,
                                        email = email
                                    )
                                    saveUserToFirestore(userToFirestore)
                                } else {
                                    showToast("Registration Failed: UID not found")
                                }
                                updateUI(user)
                            } else {
                                Log.e(TAG, "Register: Failure", task.exception)
                                val errorMessage =
                                    task.exception?.message
                                        ?: getString(R.string.authentication_failed)
                                showToast(errorMessage)
                            }
                        }
                }
            }
        }
    }

    private fun saveUserToFirestore(user: User) {
        db.collection("users").document(user.uid)
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "User profile created successfully for UID: ${user.uid}")
                showToast(getString(R.string.register_success))
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error creating user profile", e)
                showToast("Firestore Registration Failed: ${e.message}")
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