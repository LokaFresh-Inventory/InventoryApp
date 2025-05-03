package com.lokatani.lokafreshinventory.ui.chatbot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.utils.Constants.BOT
import com.lokatani.lokafreshinventory.utils.Constants.USER
import io.noties.markwon.Markwon

class ChatItemAdapter : RecyclerView.Adapter<ChatItemAdapter.ChatViewHolder>() {

    var chatList = mutableListOf<Chat>()

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatViewHolder {
        return ChatViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: ChatViewHolder,
        position: Int
    ) {
        holder.itemView.apply {
            val displayMetrics = context.resources.displayMetrics
            val maxWidth = (displayMetrics.widthPixels * 0.75).toInt() // 75% of screen
            val cvUserChat = findViewById<MaterialCardView>(R.id.cv_user_chat)
            val cvBotChat = findViewById<MaterialCardView>(R.id.cv_bot_chat)
            val tvUserChat = findViewById<TextView>(R.id.tv_user_chat)
            val tvBotChat = findViewById<TextView>(R.id.tv_bot_chat)
            val tvUserChatTime = findViewById<TextView>(R.id.tv_user_chat_time)
            val tvBotChatTime = findViewById<TextView>(R.id.tv_bot_chat_time)

            tvUserChat.maxWidth = maxWidth
            tvBotChat.maxWidth = maxWidth

            val currentChat = chatList[position]
            val markwon = Markwon.create(context)

            when (currentChat.role) {
                USER -> {
                    cvUserChat.visibility = View.VISIBLE
                    cvBotChat.visibility = View.GONE
                    tvUserChat.text = currentChat.message
                    tvUserChatTime.text = currentChat.time
                    markwon.setMarkdown(tvUserChat, currentChat.message)
                }

                BOT -> {
                    cvBotChat.visibility = View.VISIBLE
                    cvUserChat.visibility = View.GONE
                    tvBotChat.text = currentChat.message
                    tvBotChatTime.text = currentChat.time
                    markwon.setMarkdown(tvBotChat, currentChat.message)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    fun insertChat(chat: Chat) {
        this.chatList.add(chat)
        notifyItemInserted(chatList.size)
    }
}