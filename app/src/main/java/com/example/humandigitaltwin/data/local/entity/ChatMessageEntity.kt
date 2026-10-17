package com.example.humandigitaltwin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.humandigitaltwin.data.models.ChatMessage

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val message: String,
    val timestamp: String,
    val isEmergencyResponse: Boolean = false
) {
    fun toDomain(): ChatMessage = ChatMessage(
        sender = sender,
        message = message,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(msg: ChatMessage, isEmergency: Boolean = false): ChatMessageEntity = ChatMessageEntity(
            sender = msg.sender,
            message = msg.message,
            timestamp = msg.timestamp,
            isEmergencyResponse = isEmergency
        )
    }
}
