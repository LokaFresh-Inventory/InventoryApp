package com.lokatani.lokafreshinventory.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.databinding.FragmentSettingsBinding
import com.lokatani.lokafreshinventory.ui.login.LoginActivity


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.settingToolbar)
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.settings)

        binding.apply {
            btnLogout.setOnClickListener {
                Firebase.auth.signOut()
                Toast.makeText(requireContext(), "Account Logged Out", Toast.LENGTH_SHORT).show()
                val logoutIntent = Intent(requireContext(), LoginActivity::class.java)
                logoutIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(logoutIntent)
            }
        }
    }
}