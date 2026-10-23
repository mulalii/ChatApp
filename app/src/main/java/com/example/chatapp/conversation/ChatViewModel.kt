package com.example.chatapp.conversation

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val messageText: String = "",
    val timestamp: Long = 0L
)

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun listenForMessages(recipientId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatId = if (currentUserId < recipientId) {
            "${currentUserId}_$recipientId"
        } else {
            "${recipientId}_$currentUserId"
        }

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val messageList = snapshot.documents.mapNotNull { doc ->
                        val senderId = doc.getString("senderId") ?: ""
                        val text = doc.getString("messageText") ?: ""
                        val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                        Message(
                            messageId = doc.id,
                            senderId = senderId,
                            messageText = text,
                            timestamp = timestamp
                        )
                    }
                    _messages.value = messageList
                }
            }
    }

    fun sendMessage(recipientId: String, text: String) {
        if (text.isBlank()) return

        val currentUserId = auth.currentUser?.uid ?: return
        val chatId = if (currentUserId < recipientId) {
            "${currentUserId}_$recipientId"
        } else {
            "${recipientId}_$currentUserId"
        }

        val messageData = hashMapOf(
            "senderId" to currentUserId,
            "messageText" to text.trim(),
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(messageData)
    }
}
