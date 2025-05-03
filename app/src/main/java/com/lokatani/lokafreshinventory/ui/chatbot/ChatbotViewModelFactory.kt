package com.lokatani.lokafreshinventory.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lokatani.lokafreshinventory.data.remote.ChatbotRepository
import com.lokatani.lokafreshinventory.di.ChatbotInjection

class ChatbotViewModelFactory private constructor(
    private val chatbotRepository: ChatbotRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ChatbotViewModel::class.java) -> {
                return ChatbotViewModel(chatbotRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown Viewmodel Class" + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ChatbotViewModelFactory? = null
        fun getInstance(): ChatbotViewModelFactory =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChatbotViewModelFactory(ChatbotInjection.provideRepository())
            }.also { INSTANCE = it }
    }
}