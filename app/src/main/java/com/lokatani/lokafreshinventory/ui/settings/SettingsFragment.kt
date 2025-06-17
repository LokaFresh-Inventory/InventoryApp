package com.lokatani.lokafreshinventory.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.data.remote.firebase.User
import com.lokatani.lokafreshinventory.databinding.FragmentSettingsBinding
import com.lokatani.lokafreshinventory.ui.login.LoginActivity


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val languageItems = arrayOf("English", "Bahasa Indonesia")
    private val languageTags = arrayOf("en", "id")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.settingToolbar)
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.settings)

        fetchUserProfile()

        binding.apply {
            btnLogout.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.logout)
                    .setIcon(R.drawable.baseline_logout_24)
                    .setMessage(getString(R.string.are_you_sure_to_logout))
                    .setPositiveButton("YES") { _, _ ->
                        Firebase.auth.signOut()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.account_logged_out), Toast.LENGTH_SHORT
                        )
                            .show()
                        val logoutIntent = Intent(requireContext(), LoginActivity::class.java)
                        logoutIntent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(logoutIntent)
                    }
                    .setNegativeButton("NO", null) // Use null for simple dismissal
                    .show()
            }

            btnChangeLanguage.setOnClickListener {
                showChangeLanguageDialog()
            }
        }
    }

    fun fetchUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(uid)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    Log.d("Firestore", "User data fetched: ${user?.username}")

                    binding.apply {
                        tvUsername.text = user?.username
                        tvEmail.text = user?.email
                    }
                } else {
                    Log.d("Firestore", "No such document")
                    binding.apply {
                        tvUsername.text = getString(R.string.guest)
                        tvEmail.text = getString(R.string.guest_gmail_com)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "get failed with ", exception)
                binding.apply {
                    tvUsername.text = getString(R.string.guest)
                    tvEmail.text = getString(R.string.guest_gmail_com)
                }
            }
    }

    private fun showChangeLanguageDialog() {
        val currentLocale = AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()
        var checkedItem = languageTags.indexOf(currentLocale)
        if (checkedItem == -1) {
            checkedItem = 0
        }

        var selectedLanguageIndex = checkedItem

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.change_language))
            .setIcon(R.drawable.baseline_language_24)
            .setSingleChoiceItems(languageItems, checkedItem) { _, which ->
                selectedLanguageIndex = which
            }
            .setPositiveButton(getString(R.string.apply)) { dialog, _ ->
                val selectedLanguageTag = languageTags[selectedLanguageIndex]
                updateLocale(selectedLanguageTag)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateLocale(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}