package com.example.model

enum class MessageType {
    TEXT,
    FILE,
    IMAGE,
    AUDIO
}

data class P2PMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderUsername: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isMine: Boolean,
    val type: MessageType = MessageType.TEXT,
    val fileName: String? = null,
    val fileSizeFormatted: String? = null,
    val mimeType: String? = null,
    val isDelivered: Boolean = true,
    val isRead: Boolean = true
)
