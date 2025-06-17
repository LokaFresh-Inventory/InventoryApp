package com.lokatani.lokafreshinventory.ui.detail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.data.remote.firebase.User
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

        fetchUserProfile()
    }

    override fun onResume() {
        super.onResume()

        val currentTimestamp = Timestamp.now()

        val cleanVegResult = vegResult
            ?.removePrefix("Hasil API: ")
            ?.removePrefix("Hasil Local: ")
            ?.trim()

        binding.apply {
            tvVegType.text = vegResult ?: getString(R.string.no_vegetable)
            tvVegWeight.text = getString(R.string.gram, vegWeight.toString())
            tvDate.text = displayDateFormatter.format(currentTimestamp.toDate())

            buttonSave.setOnClickListener {
                detailViewModel.insertResult(
                    user = currentUser ?: getString(R.string.guest),
                    vegResult = cleanVegResult ?: getString(R.string.no_vegetable),
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

    fun fetchUserProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(uid)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    Log.d("Firestore", "User data fetched: ${user?.username}")

                    currentUser = user?.username
                } else {
                    Log.d("Firestore", "No such document")
                    currentUser = user.email
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "get failed with ", exception)
                currentUser = user.email
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