package com.lokatani.lokafreshinventory.ui.chatbot

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.utils.Constants.BOT
import com.lokatani.lokafreshinventory.utils.Constants.USER
import io.noties.markwon.Markwon

class ChatItemAdapter : RecyclerView.Adapter<ChatItemAdapter.ChatViewHolder>() {

    var chatList = mutableListOf<Chat>()

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvUserChat: MaterialCardView = itemView.findViewById(R.id.cv_user_chat)
        val cvBotChat: MaterialCardView = itemView.findViewById(R.id.cv_bot_chat)
        val tvUserChat: TextView = itemView.findViewById(R.id.tv_user_chat)
        val tvBotChat: TextView = itemView.findViewById(R.id.tv_bot_chat)
        val tvUserChatTime: TextView = itemView.findViewById(R.id.tv_user_chat_time)
        val tvBotChatTime: TextView = itemView.findViewById(R.id.tv_bot_chat_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
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

            holder.tvUserChat.maxWidth = maxWidth
            holder.tvBotChat.maxWidth = maxWidth

            val currentChat = chatList[position]
            val markwon = Markwon.create(context)

            when (currentChat.role) {
                USER -> {
                    holder.apply {
                        cvUserChat.visibility = View.VISIBLE
                        cvBotChat.visibility = View.GONE
                        tvUserChat.text = currentChat.message
                        tvUserChatTime.text = currentChat.time
                        markwon.setMarkdown(tvUserChat, currentChat.message)

                        tvUserChat.setOnLongClickListener {
                            cvUserChat.isPressed = true
                            val clipboard =
                                holder.itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("chat_message", currentChat.message)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(
                                holder.itemView.context,
                                "Text copied",
                                Toast.LENGTH_SHORT
                            ).show()
                            cvUserChat.isPressed = false
                            true
                        }
                    }
                }

                BOT -> {
                    holder.apply {
                        cvBotChat.visibility = View.VISIBLE
                        cvUserChat.visibility = View.GONE
                        tvBotChat.text = currentChat.message
                        tvBotChatTime.text = currentChat.time
                        markwon.setMarkdown(tvBotChat, currentChat.message)

                        tvBotChat.setOnLongClickListener {
                            cvBotChat.isPressed = true
                            val clipboard =
                                holder.itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("chat_message", currentChat.message)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(
                                holder.itemView.context,
                                "Text copied",
                                Toast.LENGTH_SHORT
                            ).show()
                            cvBotChat.isPressed = false
                            true
                        }
                    }
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