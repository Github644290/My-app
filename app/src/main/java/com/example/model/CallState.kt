package com.example.model

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallStatus {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTED,
    ENDED
}

data class ActiveCallSession(
    val peerUsername: String,
    val callType: CallType,
    val callStatus: CallStatus,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeaker: Boolean = true,
    val isVideoEnabled: Boolean = true,
    val isFrontCamera: Boolean = true,
    val networkQuality: String = "HD Encrypted P2P",
    val remoteAudioLevel: Float = 0.5f // For live audio wave visualizer
)
