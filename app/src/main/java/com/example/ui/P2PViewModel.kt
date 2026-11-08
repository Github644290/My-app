package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.P2PRepository
import com.example.model.ActiveCallSession
import com.example.model.AppLanguage
import com.example.model.CallType
import com.example.model.DetectionAlertType
import com.example.model.DetectionSettings
import com.example.model.DeviceSecurityLock
import com.example.model.LanguageDictionary
import com.example.model.LocalizedStrings
import com.example.model.P2PConnectionState
import com.example.model.P2PMessage
import com.example.sensor.AngryToneDetector
import com.example.sensor.DropDetector
import com.example.sensor.PowerButtonDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class P2PViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = P2PRepository(application)

    val connectionState: StateFlow<P2PConnectionState> = repository.connectionState
    val securityLock: StateFlow<DeviceSecurityLock> = repository.securityLock
    val currentLanguage: StateFlow<AppLanguage> = repository.currentLanguage
    val detectionSettings: StateFlow<DetectionSettings> = repository.detectionSettings
    val messages: StateFlow<List<P2PMessage>> = repository.messages
    val activeCall: StateFlow<ActiveCallSession?> = repository.activeCall
    val errorMessage: StateFlow<String?> = repository.errorMessage

    val localizedStrings: StateFlow<LocalizedStrings> = repository.currentLanguage
        .map { LanguageDictionary.getStrings(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            LanguageDictionary.getStrings(AppLanguage.ENGLISH)
        )

    private val _selectedTab = MutableStateFlow(0) // 0: Calling, 1: Chat/Files, 2: Regional & Settings
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Hardware Detectors
    private val dropDetector = DropDetector(
        context = application,
        onGForceUpdated = { g -> repository.updateGForce(g) },
        onDropDetected = {
            if (detectionSettings.value.dropDetectionEnabled) {
                repository.triggerAlert(DetectionAlertType.DROP_DETECTED)
            }
        }
    )

    private val angryToneDetector = AngryToneDetector(
        context = application,
        onDecibelUpdated = { db -> repository.updateAudioDb(db) },
        onAngryToneDetected = { _ ->
            if (detectionSettings.value.angryToneDetectionEnabled) {
                repository.triggerAlert(DetectionAlertType.ANGRY_TONE_DETECTED)
            }
        }
    )

    private val powerButtonDetector = PowerButtonDetector(
        context = application,
        onCountUpdated = { count -> repository.updatePowerPressCount(count) },
        onIntentionalPowerPress = {
            if (detectionSettings.value.powerButtonDetectionEnabled) {
                repository.triggerAlert(DetectionAlertType.POWER_BUTTON_EMERGENCY)
            }
        }
    )

    init {
        dropDetector.start()
        powerButtonDetector.start()
        angryToneDetector.start(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        dropDetector.stop()
        powerButtonDetector.stop()
        angryToneDetector.stop()
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun login(username: String, pass: String): Boolean {
        return repository.attemptLogin(username, pass)
    }

    fun logout() {
        repository.logout()
    }

    fun initiateCall(callType: CallType) {
        repository.initiateCall(callType)
    }

    fun simulateIncomingCall(callType: CallType) {
        repository.simulateIncomingCall(callType)
    }

    fun acceptCall() {
        repository.acceptCall()
    }

    fun endCall() {
        repository.endCall()
    }

    fun toggleMute() = repository.toggleMute()
    fun toggleSpeaker() = repository.toggleSpeaker()
    fun toggleVideo() = repository.toggleVideo()
    fun flipCamera() = repository.flipCamera()

    fun sendMessage(text: String) {
        repository.sendTextMessage(text)
    }

    fun sendFile(fileName: String, fileSize: String, mimeType: String) {
        repository.sendFile(fileName, fileSize, mimeType)
    }

    fun setLanguage(lang: AppLanguage) {
        repository.setLanguage(lang)
    }

    fun toggleDropDetection(enabled: Boolean) {
        repository.toggleDropDetection(enabled)
    }

    fun toggleAngryToneDetection(enabled: Boolean) {
        repository.toggleAngryToneDetection(enabled)
    }

    fun togglePowerButtonDetection(enabled: Boolean) {
        repository.togglePowerButtonDetection(enabled)
    }

    fun testSimulateDrop() {
        dropDetector.simulateDrop()
    }

    fun testSimulateAngryTone() {
        angryToneDetector.simulateAngryTone()
    }

    fun testSimulatePowerPress() {
        powerButtonDetector.simulatePowerPress()
    }

    fun simulateRemotePeerMatch() {
        repository.simulateRemotePeerMatch()
    }

    fun simulateRemotePeerMismatch() {
        repository.simulateRemotePeerMismatch()
    }

    fun simulateRemotePeerOffline() {
        repository.simulateRemotePeerOffline()
    }

    fun dismissAlert() {
        repository.dismissAlert()
    }

    fun clearError() {
        repository.clearErrorMessage()
    }
}
