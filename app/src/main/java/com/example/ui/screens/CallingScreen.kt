package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallType
import com.example.model.LocalizedStrings
import com.example.model.P2PConnectionState
import com.example.ui.theme.BrightGreen
import com.example.ui.theme.BrightGreenDark
import com.example.ui.theme.BrightGreenGlow
import com.example.ui.theme.BrightRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DisabledGrey
import com.example.ui.theme.SolidBlack
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WhatsAppGreen

@Composable
fun CallingScreen(
    connectionState: P2PConnectionState,
    strings: LocalizedStrings,
    onInitiateCall: (CallType) -> Unit,
    onNavigateToChat: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPaired = connectionState.isPairingSuccessful
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SolidBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App header bar with user badge and logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = if (isPaired) BrightGreen else WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = connectionState.localUsername.ifBlank { "User" },
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isPaired) "P2P Paired & Active" else "Awaiting Identical Remote Login",
                        color = if (isPaired) BrightGreen else WarningAmber,
                        fontSize = 11.sp
                    )
                }
            }

            OutlinedButton(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                modifier = Modifier.testTag("logout_button")
            ) {
                Text(strings.logoutButton, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Requirement:
        // "Until both devices successfully log in with identical credentials, all calling options must remain
        // greyed out (disabled), and a text message 'Pair not successfully' should be displayed.
        // Once paired successfully, a persistent notification must appear in the notification bar stating 'Pairing Successful'."
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPaired) SurfaceDark else SurfaceDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = if (isPaired) BrightGreen else WarningAmber,
                    shape = RoundedCornerShape(18.dp)
                )
                .testTag("pairing_status_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPaired) BrightGreen.copy(alpha = 0.15f)
                            else WarningAmber.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPaired) Icons.Default.CheckCircle else Icons.Default.SyncDisabled,
                        contentDescription = null,
                        tint = if (isPaired) BrightGreen else WarningAmber,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mandatory text display
                Text(
                    text = if (isPaired) "Pairing Successful" else "Pair not successfully",
                    color = if (isPaired) BrightGreen else WarningAmber,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.testTag("pairing_status_text")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isPaired)
                        "Peer connection established with identical credentials. Persistent notification active in notification bar."
                    else
                        "Waiting for remote device to log in with identical username & password. All call options remain greyed out.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Credential comparison telemetry box
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("This Device", color = TextMuted, fontSize = 10.sp)
                            Text(connectionState.localUsername, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Icon(
                            imageVector = if (isPaired) Icons.Default.Lock else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isPaired) BrightGreen else WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Remote Device", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = if (connectionState.remoteUsername.isNotBlank()) connectionState.remoteUsername else "Not Logged In",
                                color = if (isPaired) BrightGreen else WarningAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Calling Interface Section
        Text(
            text = "ENCRYPTED P2P CALLING",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Requirement:
        // "Call initiation buttons must be bright green. Pressing the call button triggers an incoming ring
        // and live animation on the remote device. Once the call is accepted, the green button turns red;
        // pressing this red button disconnects the call."
        // "Until both devices successfully log in with identical credentials, all calling options must remain greyed out (disabled)."
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Voice Call Card & Button
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(
                        1.dp,
                        if (isPaired) BrightGreen.copy(alpha = 0.5f) else SurfaceBorder,
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isPaired) BrightGreenGlow else SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = if (isPaired) BrightGreen else DisabledGrey,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = strings.voiceCall,
                        color = if (isPaired) TextPrimary else DisabledGrey,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (isPaired) "HD Voice • Encrypted" else "Disabled (Not Paired)",
                        color = if (isPaired) TextSecondary else DisabledGrey,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Voice Call Initiation Button: Bright green when enabled, greyed out when disabled
                    Button(
                        onClick = { onInitiateCall(CallType.VOICE) },
                        enabled = isPaired,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrightGreen,
                            contentColor = SolidBlack,
                            disabledContainerColor = DisabledGrey.copy(alpha = 0.35f),
                            disabledContentColor = DisabledGrey
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("initiate_voice_call_button")
                    ) {
                        Icon(Icons.Default.PhoneInTalk, contentDescription = "Call", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Voice", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }

            // Video Call Card & Button
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(
                        1.dp,
                        if (isPaired) BrightGreen.copy(alpha = 0.5f) else SurfaceBorder,
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isPaired) BrightGreenGlow else SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = if (isPaired) BrightGreen else DisabledGrey,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = strings.videoCall,
                        color = if (isPaired) TextPrimary else DisabledGrey,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (isPaired) "1080p P2P Video" else "Disabled (Not Paired)",
                        color = if (isPaired) TextSecondary else DisabledGrey,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Video Call Initiation Button: Bright green when enabled, greyed out when disabled
                    Button(
                        onClick = { onInitiateCall(CallType.VIDEO) },
                        enabled = isPaired,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrightGreen,
                            contentColor = SolidBlack,
                            disabledContainerColor = DisabledGrey.copy(alpha = 0.35f),
                            disabledContentColor = DisabledGrey
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("initiate_video_call_button")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Video", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // WhatsApp-style Data/File Sharing and Text Messaging Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
                .testTag("chat_file_sharing_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.chatAndFiles,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "WhatsApp-style media transfer & instant P2P messaging",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = onNavigateToChat,
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("open_chat_button")
                ) {
                    Text("Open", color = SolidBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
