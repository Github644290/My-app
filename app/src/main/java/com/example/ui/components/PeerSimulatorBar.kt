package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallType
import com.example.model.P2PConnectionState
import com.example.ui.theme.BrightGreen
import com.example.ui.theme.BrightRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun PeerSimulatorBar(
    connectionState: P2PConnectionState,
    onSimulateMatch: () -> Unit,
    onSimulateMismatch: () -> Unit,
    onSimulateOffline: () -> Unit,
    onSimulateIncomingCall: (CallType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("peer_simulator_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (connectionState.isPairingSuccessful) BrightGreen
                            else if (connectionState.isRemoteDeviceOnline) WarningAmber
                            else BrightRed
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CellTower,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "P2P Peer Tester",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (connectionState.isPairingSuccessful) "• PAIRED" else "• UNPAIRED",
                    color = if (connectionState.isPairingSuccessful) BrightGreen else WarningAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse tester" else "Expand tester",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "Simulate Device B login credentials to verify pairing gate:",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ElevatedFilterChip(
                        selected = connectionState.isPairingSuccessful,
                        onClick = onSimulateMatch,
                        label = { Text("Same Login (Pair)", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = BrightGreen.copy(alpha = 0.2f),
                            selectedLabelColor = BrightGreen
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("simulate_match_chip")
                    )

                    ElevatedFilterChip(
                        selected = connectionState.isRemoteDeviceOnline && !connectionState.isPairingSuccessful,
                        onClick = onSimulateMismatch,
                        label = { Text("Wrong Login", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = WarningAmber.copy(alpha = 0.2f),
                            selectedLabelColor = WarningAmber
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("simulate_mismatch_chip")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onSimulateIncomingCall(CallType.VOICE) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("simulate_incoming_voice_button")
                    ) {
                        Icon(Icons.Default.PhoneCallback, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trigger Call In", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onSimulateOffline,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("simulate_offline_button")
                    ) {
                        Text("Device B Offline", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
