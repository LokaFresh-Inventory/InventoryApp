package com.lokatani.lokafreshinventory.ui.chatbot

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.databinding.ActivityChatbotBinding
import com.lokatani.lokafreshinventory.utils.Constants.BOT
import com.lokatani.lokafreshinventory.utils.Constants.USER
import io.noties.markwon.Markwon
import java.text.SimpleDateFormat
import java.util.Locale

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

        val menuHost: MenuHost = this@ChatbotActivity
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.help_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_help -> {
                        val helpDialogView =
                            View.inflate(this@ChatbotActivity, R.layout.chatbot_help, null)

                        val helpDialog = MaterialAlertDialogBuilder(this@ChatbotActivity)
                            .setTitle("Welcome to Chatbot")
                            .setIcon(R.drawable.outline_robot_24)
                            .setMessage("Here you can prompt to get a summary of data you want from the database using human question")
                            .setView(helpDialogView)
                            .show()

                        val firstChoice = helpDialogView?.findViewById<Button>(R.id.choice1)
                        val secondChoice = helpDialogView?.findViewById<Button>(R.id.choice2)


                        firstChoice?.setOnClickListener {
                            binding.cvOpening.visibility = View.GONE
                            sendChatPrompt(firstChoice.text.toString())
                            helpDialog?.dismiss()
                        }

                        secondChoice?.setOnClickListener {
                            binding.cvOpening.visibility = View.GONE
                            sendChatPrompt(secondChoice.text.toString())
                            helpDialog?.dismiss()
                        }
                        true
                    }

                    else -> false
                }
            }
        }, this@ChatbotActivity, Lifecycle.State.RESUMED)

        recycleViewSetup()
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            edChat.doAfterTextChanged { text ->
                val prompt = text.toString().trim()
                btnSend.isEnabled = prompt.isNotEmpty()
            }

            btnSend.setOnClickListener {
                cvOpening.visibility = View.GONE
                val prompt = edChat.text.toString().trim()
                sendChatPrompt(prompt)
            }
        }
    }

    private fun sendChatPrompt(prompt: String) {
        val timestamp = Timestamp.now()
        val convertedTimestamp: String =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate())
        binding.apply {
            adapter.insertChat(Chat(prompt, USER, convertedTimestamp))
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
                            adapter.insertChat(Chat(resultData, BOT, convertedTimestamp))
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