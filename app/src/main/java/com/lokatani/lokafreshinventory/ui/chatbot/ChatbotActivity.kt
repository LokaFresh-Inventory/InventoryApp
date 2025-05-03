package com.lokatani.lokafreshinventory.ui.chatbot

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.databinding.ActivityChatbotBinding
import com.lokatani.lokafreshinventory.utils.Constants.BOT
import com.lokatani.lokafreshinventory.utils.Constants.USER
import com.lokatani.lokafreshinventory.utils.DateUtils
import io.noties.markwon.Markwon

class ChatbotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatbotBinding

    private lateinit var adapter: ChatItemAdapter

    private lateinit var factory: ChatbotViewModelFactory
    private val chatbotViewModel: ChatbotViewModel by viewModels {
        factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        factory = ChatbotViewModelFactory.getInstance()

        setSupportActionBar(binding.chatbotToolbar)
        supportActionBar?.title = "Chatbot"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recycleViewSetup()
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            edChat.doAfterTextChanged { text ->
                val prompt = text.toString().trim()
                btnSend.isEnabled = prompt.isNotEmpty()
            }

            val timestamp = DateUtils.timeStamp()

            btnSend.setOnClickListener {
                val prompt = edChat.text.toString().trim()
                adapter.insertChat(Chat(prompt, USER, timestamp))
                rvChatbot.scrollToPosition(adapter.itemCount - 1)
                edChat.setText("")

                chatbotViewModel.sendChat(prompt).observe(this@ChatbotActivity) { result ->
                    if (result != null) {
                        when (result) {
                            is Result.Loading -> {
                                showLoading(true)
                                tvErrorChat.visibility = View.GONE
                            }

                            is Result.Success -> {
                                showLoading(false)
                                val resultData = result.data.output.orEmpty()
                                adapter.insertChat(Chat(resultData, BOT, timestamp))
                                rvChatbot.scrollToPosition(adapter.itemCount - 1)
                            }

                            is Result.Error -> {
                                showLoading(false)
                                val errorText = "**Error**. Check your connection or try again"
                                val markwon = Markwon.create(this@ChatbotActivity)
                                tvErrorChat.apply {
                                    visibility = View.VISIBLE
                                    markwon.setMarkdown(this, errorText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun recycleViewSetup() {
        adapter = ChatItemAdapter()
        binding.rvChatbot.adapter = adapter
        binding.rvChatbot.layoutManager = LinearLayoutManager(this@ChatbotActivity)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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
}