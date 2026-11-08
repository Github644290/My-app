package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveCallSession
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.model.LocalizedStrings
import com.example.ui.theme.BrightGreen
import com.example.ui.theme.BrightGreenGlow
import com.example.ui.theme.BrightRed
import com.example.ui.theme.BrightRedDark
import com.example.ui.theme.BrightRedGlow
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SolidBlack
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun ActiveCallOverlay(
    callSession: ActiveCallSession?,
    strings: LocalizedStrings,
    onAccept: () -> Unit,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleVideo: () -> Unit,
    onFlipCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (callSession == null) return

    val infiniteTransition = rememberInfiniteTransition(label = "call_ring_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val visualizerWavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SolidBlack.copy(alpha = 0.96f))
            .testTag("active_call_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = BrightGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "END-TO-END ENCRYPTED P2P",
                        color = BrightGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = callSession.peerUsername.ifBlank { "Remote Peer" },
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                val statusText = when (callSession.callStatus) {
                    CallStatus.OUTGOING_RINGING -> "Calling remote device... (Ringing)"
                    CallStatus.INCOMING_RINGING -> strings.incomingCall
                    CallStatus.CONNECTED -> {
                        val mins = callSession.durationSeconds / 60
                        val secs = callSession.durationSeconds % 60
                        String.format("%02d:%02d • HD P2P Stream", mins, secs)
                    }
                    CallStatus.ENDED -> "Call Disconnected"
                    CallStatus.IDLE -> ""
                }

                Text(
                    text = statusText,
                    color = if (callSession.callStatus == CallStatus.CONNECTED) BrightGreen else CyberCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Center Content: Video Feed or Audio Wave Visualizer
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (callSession.callType == CallType.VIDEO && callSession.callStatus == CallStatus.CONNECTED) {
                    // Video Call Presentation
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDark)
                            .border(2.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    ) {
                        // Main simulated remote camera feed
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // High-tech futuristic radar grid overlay
                            val w = size.width
                            val h = size.height
                            drawLine(
                                color = CyberCyan.copy(alpha = 0.15f),
                                start = Offset(0f, h / 2),
                                end = Offset(w, h / 2),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = CyberCyan.copy(alpha = 0.15f),
                                start = Offset(w / 2, 0f),
                                end = Offset(w / 2, h),
                                strokeWidth = 2f
                            )
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Live P2P Video Stream Active",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Camera: Remote Front Lens (1080p 60fps)",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        // Local Picture-in-Picture window
                        Box(
                            modifier = Modifier
                                .size(width = 100.dp, height = 140.dp)
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SolidBlack)
                                .border(1.5.dp, BrightGreen, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Local Cam", color = BrightGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Icon(
                                    imageVector = if (callSession.isFrontCamera) Icons.Default.Cameraswitch else Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Voice Call or Ringing State with animated pulsing visualizer
                    Box(
                        modifier = Modifier.size(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer glowing pulse ring
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        if (callSession.callStatus == CallStatus.INCOMING_RINGING) WarningAmber.copy(alpha = 0.25f)
                                        else BrightGreenGlow,
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension / 2 * pulseScale
                            )
                        }

                        // Center Avatar & Wave Visualizer
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceDark)
                                    .border(
                                        3.dp,
                                        if (callSession.callStatus == CallStatus.CONNECTED) BrightGreen else CyberCyan,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (callSession.callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.PhoneInTalk,
                                    contentDescription = null,
                                    tint = if (callSession.callStatus == CallStatus.CONNECTED) BrightGreen else CyberCyan,
                                    modifier = Modifier.size(52.dp)
                                )
                            }

                            if (callSession.callStatus == CallStatus.CONNECTED) {
                                Spacer(modifier = Modifier.height(20.dp))
                                // Live dynamic audio bars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val barHeights = listOf(14.dp, 28.dp, 44.dp, 32.dp, 50.dp, 36.dp, 22.dp)
                                    barHeights.forEachIndexed { idx, height ->
                                        val multiplier = if (idx % 2 == 0) callSession.remoteAudioLevel else (1f - callSession.remoteAudioLevel * 0.5f)
                                        Box(
                                            modifier = Modifier
                                                .width(6.dp)
                                                .height(height * multiplier.coerceIn(0.4f, 1.3f))
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(BrightGreen)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (callSession.callStatus == CallStatus.INCOMING_RINGING) {
                    // Incoming call: Bright green accept and red decline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decline Button (Red)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FloatingActionButton(
                                onClick = onEndCall,
                                containerColor = BrightRed,
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(68.dp)
                                    .testTag("incoming_decline_button"),
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                            ) {
                                Icon(Icons.Default.CallEnd, contentDescription = "Decline", modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(strings.declineCall, color = TextSecondary, fontSize = 13.sp)
                        }

                        // Accept Button (Bright Green)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FloatingActionButton(
                                onClick = onAccept,
                                containerColor = BrightGreen,
                                contentColor = SolidBlack,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(68.dp)
                                    .testTag("incoming_accept_button"),
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Accept", modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(strings.acceptCall, color = BrightGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Active or Outgoing Call Controls
                    if (callSession.callStatus == CallStatus.CONNECTED) {
                        // In-call toggles
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(
                                onClick = onToggleMute,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (callSession.isMuted) BrightRed.copy(alpha = 0.2f) else SurfaceElevated)
                                    .testTag("mute_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (callSession.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Toggle Mute",
                                    tint = if (callSession.isMuted) BrightRed else TextPrimary
                                )
                            }

                            IconButton(
                                onClick = onToggleSpeaker,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (callSession.isSpeaker) CyberCyan.copy(alpha = 0.2f) else SurfaceElevated)
                                    .testTag("speaker_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (callSession.isSpeaker) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                    contentDescription = "Toggle Speaker",
                                    tint = if (callSession.isSpeaker) CyberCyan else TextPrimary
                                )
                            }

                            if (callSession.callType == CallType.VIDEO) {
                                IconButton(
                                    onClick = onFlipCamera,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceElevated)
                                        .testTag("flip_camera_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cameraswitch,
                                        contentDescription = "Flip Camera",
                                        tint = TextPrimary
                                    )
                                }

                                IconButton(
                                    onClick = onToggleVideo,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(if (!callSession.isVideoEnabled) BrightRed.copy(alpha = 0.2f) else SurfaceElevated)
                                        .testTag("video_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (callSession.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                        contentDescription = "Toggle Video",
                                        tint = if (!callSession.isVideoEnabled) BrightRed else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Requirement:
                    // "Once the call is accepted, the green button turns red; pressing this red button disconnects the call."
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FloatingActionButton(
                            onClick = onEndCall,
                            containerColor = BrightRed,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(68.dp)
                                .testTag("disconnect_call_button"),
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "Disconnect Call", modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(strings.endCall, color = BrightRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
