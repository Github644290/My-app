package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.DetectionSettings
import com.example.model.DeviceSecurityLock
import com.example.model.LocalizedStrings
import com.example.ui.theme.BrightGreen
import com.example.ui.theme.BrightRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SolidBlack
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    currentLanguage: AppLanguage,
    detectionSettings: DetectionSettings,
    securityLock: DeviceSecurityLock,
    strings: LocalizedStrings,
    onLanguageSelected: (AppLanguage) -> Unit,
    onToggleDropDetection: (Boolean) -> Unit,
    onToggleAngryToneDetection: (Boolean) -> Unit,
    onTogglePowerButtonDetection: (Boolean) -> Unit,
    onTestDrop: () -> Unit,
    onTestAngryTone: () -> Unit,
    onTestPowerButton: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SolidBlack)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Settings Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CyberCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = strings.settingsTitle,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Regional Languages & Hardware AI Sensors",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: Indian Regional Languages
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
                .testTag("language_selection_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = BrightGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = strings.languageSection,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Select an Indian regional language to dynamically translate the app interface:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = lang == currentLanguage
                        ElevatedFilterChip(
                            selected = isSelected,
                            onClick = { onLanguageSelected(lang) },
                            label = {
                                Text(
                                    text = "${lang.nativeName} (${lang.englishName})",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                containerColor = SurfaceElevated,
                                labelColor = TextPrimary,
                                selectedContainerColor = BrightGreen.copy(alpha = 0.25f),
                                selectedLabelColor = BrightGreen
                            ),
                            modifier = Modifier.testTag("lang_chip_${lang.code}")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 2: Three Required Detection Features & Toggles
        Text(
            text = "HARDWARE & SENSOR DETECTION TOGGLES",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Feature 1: Mobile Drop Detection
        DetectionToggleCard(
            title = strings.dropDetectionTitle,
            description = strings.dropDetectionDesc,
            icon = Icons.Default.ScreenRotation,
            iconTint = BrightRed,
            isEnabled = detectionSettings.dropDetectionEnabled,
            onToggle = onToggleDropDetection,
            telemetryLabel = "Current G-Force: %.2f G".format(detectionSettings.currentGForce),
            telemetryProgress = (detectionSettings.currentGForce / 3.0f).coerceIn(0f, 1f),
            testButtonText = "Simulate Drop Spike",
            onTestClick = onTestDrop,
            testTagPrefix = "drop_detection"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Feature 2: Angry Tone Detection
        DetectionToggleCard(
            title = strings.angryToneTitle,
            description = strings.angryToneDesc,
            icon = Icons.Default.Hearing,
            iconTint = WarningAmber,
            isEnabled = detectionSettings.angryToneDetectionEnabled,
            onToggle = onToggleAngryToneDetection,
            telemetryLabel = "Voice Stress Level: %.1f dB".format(detectionSettings.currentAudioDb),
            telemetryProgress = ((detectionSettings.currentAudioDb - 20f) / 70f).coerceIn(0f, 1f),
            testButtonText = "Simulate Angry Tone",
            onTestClick = onTestAngryTone,
            testTagPrefix = "angry_tone"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Feature 3: Intentional Power Button Press Detection
        DetectionToggleCard(
            title = strings.powerButtonTitle,
            description = strings.powerButtonDesc,
            icon = Icons.Default.PowerSettingsNew,
            iconTint = CyberCyan,
            isEnabled = detectionSettings.powerButtonDetectionEnabled,
            onToggle = onTogglePowerButtonDetection,
            telemetryLabel = "Rapid Presses Detected: ${detectionSettings.powerPressCount} / 3",
            telemetryProgress = (detectionSettings.powerPressCount / 3.0f).coerceIn(0f, 1f),
            testButtonText = "Simulate Power Press SOS",
            onTestClick = onTestPowerButton,
            testTagPrefix = "power_button"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Device Security & IMEI Lock Info
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Security, null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                    Text("Device Hardware IMEI Profile", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Hardware IMEI / Fingerprint: ${securityLock.deviceImeiOrId}", color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Text(
                    text = if (securityLock.isPeriodActive)
                        "15-Day Account Lock: ACTIVE for user '${securityLock.lockedUsername}'\nExpires in: ${securityLock.remainingFormatted}"
                    else
                        "15-Day Lock: Available for new account registration",
                    color = if (securityLock.isPeriodActive) WarningAmber else TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun DetectionToggleCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    telemetryLabel: String,
    telemetryProgress: Float,
    testButtonText: String,
    onTestClick: () -> Unit,
    testTagPrefix: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isEnabled) iconTint.copy(alpha = 0.5f) else SurfaceBorder,
                RoundedCornerShape(18.dp)
            )
            .testTag("${testTagPrefix}_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEnabled) "Active Monitoring" else "Disabled",
                            color = if (isEnabled) BrightGreen else TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SolidBlack,
                        checkedTrackColor = iconTint,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceElevated
                    ),
                    modifier = Modifier.testTag("${testTagPrefix}_switch")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Telemetry progress bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(telemetryLabel, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text(if (isEnabled) "ONLINE" else "PAUSED", color = if (isEnabled) BrightGreen else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { telemetryProgress },
                    color = iconTint,
                    trackColor = SurfaceBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Test button
            OutlinedButton(
                onClick = onTestClick,
                enabled = isEnabled,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = iconTint),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${testTagPrefix}_test_button")
            ) {
                Text(testButtonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
