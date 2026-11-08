package com.example.model

enum class DetectionAlertType {
    DROP_DETECTED,
    ANGRY_TONE_DETECTED,
    POWER_BUTTON_EMERGENCY
}

data class DetectionSettings(
    val dropDetectionEnabled: Boolean = true,
    val angryToneDetectionEnabled: Boolean = true,
    val powerButtonDetectionEnabled: Boolean = true,
    val currentGForce: Float = 1.0f,
    val currentAudioDb: Float = 32.0f,
    val powerPressCount: Int = 0,
    val activeAlertType: DetectionAlertType? = null,
    val alertTimestamp: Long = 0L
)

data class P2PConnectionState(
    val localUsername: String = "",
    val localPassword: String = "",
    val remoteUsername: String = "",
    val remotePassword: String = "",
    val isRemoteDeviceOnline: Boolean = true,
    val isLocalLoggedIn: Boolean = false,
    val remoteDeviceModel: String = "Remote P2P Peer Node"
) {
    /**
     * Requirement:
     * For successful pairing, both devices must log in using the exact same username and password.
     * Until both devices successfully log in with identical credentials,
     * all calling options must remain greyed out (disabled), and a text message "Pair not successfully" should be displayed.
     */
    val isPairingSuccessful: Boolean
        get() = isLocalLoggedIn &&
                isRemoteDeviceOnline &&
                localUsername.isNotBlank() &&
                localPassword.isNotBlank() &&
                remoteUsername.isNotBlank() &&
                remotePassword.isNotBlank() &&
                localUsername == remoteUsername &&
                localPassword == remotePassword
}
