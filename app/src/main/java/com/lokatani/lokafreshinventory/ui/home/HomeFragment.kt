package com.lokatani.lokafreshinventory.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lokatani.lokafreshinventory.databinding.FragmentHomeBinding
import com.lokatani.lokafreshinventory.ui.chatbot.ChatbotActivity
import com.lokatani.lokafreshinventory.ui.history.HistoryActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnChatbot.setOnClickListener {
                startActivity(Intent(requireContext(), ChatbotActivity::class.java))
            }
            btnHistory.setOnClickListener {
                startActivity(Intent(requireContext(), HistoryActivity::class.java))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
