package com.lokatani.lokafreshinventory.ui.chatbot

import androidx.lifecycle.ViewModel
import com.lokatani.lokafreshinventory.data.remote.ChatbotRepository

class ChatbotViewModel(
    private val chatbotRepository: ChatbotRepository
) : ViewModel() {
    fun sendChat(
        prompt: String
    ) = chatbotRepository.sendChat(prompt)

}