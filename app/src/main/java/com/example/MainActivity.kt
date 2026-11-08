package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.P2PViewModel
import com.example.ui.components.ActiveCallOverlay
import com.example.ui.components.DetectionAlertDialog
import com.example.ui.components.PeerSimulatorBar
import com.example.ui.screens.CallingScreen
import com.example.ui.screens.ChatAndFilesScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BrightGreen
import com.example.ui.theme.BrightRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SolidBlack
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WhatsAppGreen

class MainActivity : ComponentActivity() {

    private val viewModel: P2PViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Request relevant runtime permissions for notifications, microphone, camera, phone state
                val permissionsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { /* Permissions evaluated */ }

                LaunchedEffect(Unit) {
                    val permissionsToRequest = mutableListOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                        permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE)
                    }

                    val ungranted = permissionsToRequest.filter {
                        ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                    }
                    if (ungranted.isNotEmpty()) {
                        permissionsLauncher.launch(ungranted.toTypedArray())
                    }
                }

                P2PAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun P2PAppContent(viewModel: P2PViewModel) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val securityLock by viewModel.securityLock.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val strings by viewModel.localizedStrings.collectAsStateWithLifecycle()
    val detectionSettings by viewModel.detectionSettings.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    BackHandler(enabled = selectedTab != 0) {
        viewModel.setSelectedTab(0)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SolidBlack)
            .statusBarsPadding(),
        containerColor = SolidBlack,
        topBar = {
            if (connectionState.isLocalLoggedIn) {
                PeerSimulatorBar(
                    connectionState = connectionState,
                    onSimulateMatch = { viewModel.simulateRemotePeerMatch() },
                    onSimulateMismatch = { viewModel.simulateRemotePeerMismatch() },
                    onSimulateOffline = { viewModel.simulateRemotePeerOffline() },
                    onSimulateIncomingCall = { type -> viewModel.simulateIncomingCall(type) }
                )
            }
        },
        bottomBar = {
            if (connectionState.isLocalLoggedIn && activeCall == null) {
                NavigationBar(
                    containerColor = SurfaceDark,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("bottom_navigation_bar"),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Calls"
                            )
                        },
                        label = { Text("Calls", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SolidBlack,
                            selectedTextColor = BrightGreen,
                            indicatorColor = BrightGreen,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_calls")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat & Files"
                            )
                        },
                        label = { Text("Chat & Files", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SolidBlack,
                            selectedTextColor = WhatsAppGreen,
                            indicatorColor = WhatsAppGreen,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_chat")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { viewModel.setSelectedTab(2) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SolidBlack,
                            selectedTextColor = BrightGreen,
                            indicatorColor = BrightGreen,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!connectionState.isLocalLoggedIn) {
                // Requirement 3:
                // Login Screen: Must have a solid black background, with input text fields
                // for the username and password rendered in bright red.
                LoginScreen(
                    securityLock = securityLock,
                    strings = strings,
                    errorMessage = errorMessage,
                    onLogin = { user, pass ->
                        viewModel.login(user, pass)
                    }
                )
            } else {
                when (selectedTab) {
                    0 -> CallingScreen(
                        connectionState = connectionState,
                        strings = strings,
                        onInitiateCall = { callType -> viewModel.initiateCall(callType) },
                        onNavigateToChat = { viewModel.setSelectedTab(1) },
                        onLogout = { viewModel.logout() }
                    )
                    1 -> ChatAndFilesScreen(
                        messages = messages,
                        strings = strings,
                        peerUsername = connectionState.remoteUsername,
                        onSendMessage = { text -> viewModel.sendMessage(text) },
                        onSendFile = { name, size, mime -> viewModel.sendFile(name, size, mime) },
                        onBack = { viewModel.setSelectedTab(0) }
                    )
                    2 -> SettingsScreen(
                        currentLanguage = currentLanguage,
                        detectionSettings = detectionSettings,
                        securityLock = securityLock,
                        strings = strings,
                        onLanguageSelected = { lang -> viewModel.setLanguage(lang) },
                        onToggleDropDetection = { enabled -> viewModel.toggleDropDetection(enabled) },
                        onToggleAngryToneDetection = { enabled -> viewModel.toggleAngryToneDetection(enabled) },
                        onTogglePowerButtonDetection = { enabled -> viewModel.togglePowerButtonDetection(enabled) },
                        onTestDrop = { viewModel.testSimulateDrop() },
                        onTestAngryTone = { viewModel.testSimulateAngryTone() },
                        onTestPowerButton = { viewModel.testSimulatePowerPress() }
                    )
                }
            }

            // Active call overlay (video, voice, ringing, live animations, red disconnect button)
            if (activeCall != null) {
                ActiveCallOverlay(
                    callSession = activeCall,
                    strings = strings,
                    onAccept = { viewModel.acceptCall() },
                    onEndCall = { viewModel.endCall() },
                    onToggleMute = { viewModel.toggleMute() },
                    onToggleSpeaker = { viewModel.toggleSpeaker() },
                    onToggleVideo = { viewModel.toggleVideo() },
                    onFlipCamera = { viewModel.flipCamera() }
                )
            }

            // Detection Alert Modals (Drop, Angry Tone, Power Button)
            DetectionAlertDialog(
                alertType = detectionSettings.activeAlertType,
                strings = strings,
                onDismiss = { viewModel.dismissAlert() }
            )
        }
    }
}
