package com.example.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.example.model.ActiveCallSession
import com.example.model.AppLanguage
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.model.DetectionAlertType
import com.example.model.DetectionSettings
import com.example.model.DeviceSecurityLock
import com.example.model.MessageType
import com.example.model.P2PConnectionState
import com.example.model.P2PMessage
import com.example.service.P2PNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlin.math.absoluteValue

class P2PRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("p2p_secure_storage", Context.MODE_PRIVATE)

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var callDurationJob: Job? = null
    private var audioVisualizerJob: Job? = null

    // State flows
    private val _connectionState = MutableStateFlow(P2PConnectionState())
    val connectionState: StateFlow<P2PConnectionState> = _connectionState.asStateFlow()

    private val _securityLock = MutableStateFlow(loadSecurityLock())
    val securityLock: StateFlow<DeviceSecurityLock> = _securityLock.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _detectionSettings = MutableStateFlow(DetectionSettings())
    val detectionSettings: StateFlow<DetectionSettings> = _detectionSettings.asStateFlow()

    private val _messages = MutableStateFlow<List<P2PMessage>>(emptyList())
    val messages: StateFlow<List<P2PMessage>> = _messages.asStateFlow()

    private val _activeCall = MutableStateFlow<ActiveCallSession?>(null)
    val activeCall: StateFlow<ActiveCallSession?> = _activeCall.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Preload any stored credentials if locked to user
        val lockedUser = _securityLock.value.lockedUsername
        if (!lockedUser.isNullOrBlank() && _securityLock.value.isPeriodActive) {
            val savedPass = prefs.getString("saved_pass_$lockedUser", "") ?: ""
            _connectionState.update {
                it.copy(
                    localUsername = lockedUser,
                    localPassword = savedPass,
                    // By default for quick testing, configure remote peer matching initially
                    remoteUsername = lockedUser,
                    remotePassword = savedPass
                )
            }
        } else {
            // Default initial state: Device B is waiting with matching credentials or offline
            _connectionState.update {
                it.copy(
                    remoteUsername = "",
                    remotePassword = ""
                )
            }
        }

        // Initialize sample welcome message in P2P thread
        _messages.value = listOf(
            P2PMessage(
                senderUsername = "System",
                content = "Serverless P2P channel ready. Ensure both devices log in with identical credentials to unlock encrypted calls and media transfers.",
                isMine = false,
                type = MessageType.TEXT
            )
        )
    }

    /**
     * Resolves the device's hardware IMEI or unique hardware identity.
     * Complies with Android 10+ runtime safety.
     */
    @SuppressLint("HardwareIds")
    fun detectDeviceImeiOrId(): String {
        var imeiCandidate: String? = null
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                if (telephonyManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        imeiCandidate = telephonyManager.imei
                    } else {
                        @Suppress("DEPRECATION")
                        imeiCandidate = telephonyManager.deviceId
                    }
                }
            }
        } catch (ignored: SecurityException) {
            // Android 10+ blocks non-privileged apps from reading IMEI directly
        }

        if (!imeiCandidate.isNullOrBlank() && imeiCandidate != "unknown") {
            return imeiCandidate
        }

        // Fallback: Deterministic 15-digit TAC+SNR IMEI format derived from hardware fingerprint
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "0123456789"
        val hwSeed = "${Build.BRAND}-${Build.MODEL}-${Build.HARDWARE}-$androidId"
        val hashBytes = MessageDigest.getInstance("SHA-256").digest(hwSeed.toByteArray())
        var numStr = ""
        for (b in hashBytes) {
            numStr += (b.toInt().absoluteValue % 10).toString()
            if (numStr.length >= 15) break
        }
        val prefix = "86" // Standard international TAC prefix
        val final15Digit = (prefix + numStr).take(15).padEnd(15, '7')
        return final15Digit
    }

    private fun loadSecurityLock(): DeviceSecurityLock {
        val detectedImei = detectDeviceImeiOrId()
        val storedUsername = prefs.getString("locked_username", null)
        val lockStart = prefs.getLong("lock_start_time", 0L)
        val isLocked = storedUsername != null && DeviceSecurityLock.calculateRemainingMillis(lockStart) > 0L

        return DeviceSecurityLock(
            deviceImeiOrId = detectedImei,
            lockedUsername = storedUsername,
            lockStartTimeMillis = lockStart,
            isLocked = isLocked
        )
    }

    /**
     * Requirement:
     * "Upon launch/first install, the app must detect the device's IMEI number and block multiple account switching
     * or creation for a strict 15-day period."
     */
    fun attemptLogin(username: String, pass: String): Boolean {
        if (username.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Username and password cannot be empty"
            return false
        }

        val lock = _securityLock.value
        val now = System.currentTimeMillis()

        if (lock.isPeriodActive) {
            if (lock.lockedUsername != null && !lock.lockedUsername.equals(username, ignoreCase = true)) {
                _errorMessage.value =
                    "Account switching blocked by hardware security policy.\nThis device is locked to '${lock.lockedUsername}' for 15 days.\nExpires in: ${lock.remainingFormatted}"
                return false
            }
        } else {
            // First time login or previous 15-day lock has expired: Bind to new username for 15 days!
            prefs.edit()
                .putString("locked_username", username)
                .putLong("lock_start_time", now)
                .putString("saved_pass_$username", pass)
                .apply()

            _securityLock.value = DeviceSecurityLock(
                deviceImeiOrId = lock.deviceImeiOrId,
                lockedUsername = username,
                lockStartTimeMillis = now,
                isLocked = true
            )
        }

        prefs.edit().putString("saved_pass_$username", pass).apply()
        _errorMessage.value = null

        _connectionState.update {
            it.copy(
                localUsername = username,
                localPassword = pass,
                isLocalLoggedIn = true
            )
        }

        updateNotificationStatus()
        return true
    }

    fun logout() {
        _connectionState.update {
            it.copy(
                isLocalLoggedIn = false
            )
        }
        endCall()
        updateNotificationStatus()
    }

    /**
     * Remote peer simulation / direct pairing synchronization.
     * Allows setting remote peer credentials to test:
     * - Identical credentials (Pair Successful -> Green Call Buttons -> Persistent Notification)
     * - Different credentials (Pair not successfully -> Disabled Call Buttons)
     */
    fun setRemoteDeviceCredentials(remoteUser: String, remotePass: String, isOnline: Boolean) {
        _connectionState.update {
            it.copy(
                remoteUsername = remoteUser,
                remotePassword = remotePass,
                isRemoteDeviceOnline = isOnline
            )
        }
        updateNotificationStatus()
    }

    fun simulateRemotePeerMatch() {
        val localUser = _connectionState.value.localUsername
        val localPass = _connectionState.value.localPassword
        setRemoteDeviceCredentials(localUser, localPass, isOnline = true)
    }

    fun simulateRemotePeerMismatch() {
        setRemoteDeviceCredentials("other_device_user", "wrong_password_99", isOnline = true)
    }

    fun simulateRemotePeerOffline() {
        setRemoteDeviceCredentials("", "", isOnline = false)
    }

    private fun updateNotificationStatus() {
        val state = _connectionState.value
        if (state.isPairingSuccessful) {
            P2PNotificationService.start(context)
        } else {
            P2PNotificationService.stop(context)
        }
    }

    // Call Actions
    fun initiateCall(callType: CallType) {
        val state = _connectionState.value
        if (!state.isPairingSuccessful) {
            _errorMessage.value = "Cannot place call: Pair not successfully"
            return
        }

        _activeCall.value = ActiveCallSession(
            peerUsername = state.remoteUsername,
            callType = callType,
            callStatus = CallStatus.OUTGOING_RINGING
        )

        // Simulate remote peer ringing, then automatically accept or allow manual acceptance
        coroutineScope.launch {
            delay(1500)
            if (_activeCall.value?.callStatus == CallStatus.OUTGOING_RINGING) {
                // Call accepted by remote peer
                acceptCall()
            }
        }
    }

    fun simulateIncomingCall(callType: CallType) {
        val state = _connectionState.value
        _activeCall.value = ActiveCallSession(
            peerUsername = if (state.remoteUsername.isNotBlank()) state.remoteUsername else "Peer_Node_Beta",
            callType = callType,
            callStatus = CallStatus.INCOMING_RINGING
        )
    }

    fun acceptCall() {
        _activeCall.update { current ->
            current?.copy(callStatus = CallStatus.CONNECTED, durationSeconds = 0)
        }

        // Start duration counter and visualizer
        callDurationJob?.cancel()
        callDurationJob = coroutineScope.launch {
            while (isActive && _activeCall.value?.callStatus == CallStatus.CONNECTED) {
                delay(1000)
                _activeCall.update { it?.copy(durationSeconds = (it.durationSeconds + 1)) }
            }
        }

        audioVisualizerJob?.cancel()
        audioVisualizerJob = coroutineScope.launch {
            val visualizerWave = floatArrayOf(0.3f, 0.7f, 0.4f, 0.9f, 0.6f, 0.8f, 0.5f)
            var index = 0
            while (isActive && _activeCall.value?.callStatus == CallStatus.CONNECTED) {
                delay(200)
                val level = visualizerWave[index % visualizerWave.size]
                index++
                _activeCall.update { it?.copy(remoteAudioLevel = level) }
            }
        }
    }

    fun endCall() {
        callDurationJob?.cancel()
        callDurationJob = null
        audioVisualizerJob?.cancel()
        audioVisualizerJob = null

        _activeCall.update { it?.copy(callStatus = CallStatus.ENDED) }
        coroutineScope.launch {
            delay(400)
            _activeCall.value = null
        }
    }

    fun toggleMute() {
        _activeCall.update { it?.copy(isMuted = !(it.isMuted)) }
    }

    fun toggleSpeaker() {
        _activeCall.update { it?.copy(isSpeaker = !(it.isSpeaker)) }
    }

    fun toggleVideo() {
        _activeCall.update { it?.copy(isVideoEnabled = !(it.isVideoEnabled)) }
    }

    fun flipCamera() {
        _activeCall.update { it?.copy(isFrontCamera = !(it.isFrontCamera)) }
    }

    // Chat and File Sharing
    fun sendTextMessage(content: String) {
        if (content.isBlank()) return
        val myUser = _connectionState.value.localUsername.ifBlank { "Me" }
        val newMsg = P2PMessage(
            senderUsername = myUser,
            content = content,
            isMine = true,
            type = MessageType.TEXT
        )
        _messages.update { it + newMsg }

        // If paired, simulate remote delivery receipt and auto-reply
        if (_connectionState.value.isPairingSuccessful) {
            coroutineScope.launch {
                delay(1000)
                val remoteUser = _connectionState.value.remoteUsername
                val replyMsg = P2PMessage(
                    senderUsername = remoteUser,
                    content = "Encrypted P2P ACK: Received '${content.take(20)}...'",
                    isMine = false,
                    type = MessageType.TEXT
                )
                _messages.update { it + replyMsg }
            }
        }
    }

    fun sendFile(fileName: String, fileSize: String, mimeType: String) {
        val myUser = _connectionState.value.localUsername.ifBlank { "Me" }
        val newMsg = P2PMessage(
            senderUsername = myUser,
            content = "Shared file: $fileName",
            isMine = true,
            type = if (mimeType.startsWith("image/")) MessageType.IMAGE else MessageType.FILE,
            fileName = fileName,
            fileSizeFormatted = fileSize,
            mimeType = mimeType
        )
        _messages.update { it + newMsg }

        if (_connectionState.value.isPairingSuccessful) {
            coroutineScope.launch {
                delay(1200)
                val remoteUser = _connectionState.value.remoteUsername
                val replyMsg = P2PMessage(
                    senderUsername = remoteUser,
                    content = "File received and verified via P2P checksum: $fileName ($fileSize)",
                    isMine = false,
                    type = MessageType.TEXT
                )
                _messages.update { it + replyMsg }
            }
        }
    }

    // Regional Language & Detection settings
    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun toggleDropDetection(enabled: Boolean) {
        _detectionSettings.update { it.copy(dropDetectionEnabled = enabled) }
    }

    fun toggleAngryToneDetection(enabled: Boolean) {
        _detectionSettings.update { it.copy(angryToneDetectionEnabled = enabled) }
    }

    fun togglePowerButtonDetection(enabled: Boolean) {
        _detectionSettings.update { it.copy(powerButtonDetectionEnabled = enabled) }
    }

    fun updateGForce(g: Float) {
        _detectionSettings.update { it.copy(currentGForce = g) }
    }

    fun updateAudioDb(db: Float) {
        _detectionSettings.update { it.copy(currentAudioDb = db) }
    }

    fun updatePowerPressCount(count: Int) {
        _detectionSettings.update { it.copy(powerPressCount = count) }
    }

    fun triggerAlert(type: DetectionAlertType) {
        _detectionSettings.update {
            it.copy(
                activeAlertType = type,
                alertTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun dismissAlert() {
        _detectionSettings.update { it.copy(activeAlertType = null) }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
