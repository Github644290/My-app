package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DetectionAlertType
import com.example.model.LocalizedStrings
import com.example.ui.theme.BrightGreen
import com.example.ui.theme.BrightRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun DetectionAlertDialog(
    alertType: DetectionAlertType?,
    strings: LocalizedStrings,
    onDismiss: () -> Unit
) {
    if (alertType == null) return

    val (title, message, icon, accentColor) = when (alertType) {
        DetectionAlertType.DROP_DETECTED -> Quad(
            strings.dropAlertTitle,
            strings.dropAlertMessage + "\n\nAccelerometer telemetry: Accidental free-fall and impact detected. Automatic safety notification dispatched to paired device.",
            Icons.Default.ScreenRotation,
            BrightRed
        )
        DetectionAlertType.ANGRY_TONE_DETECTED -> Quad(
            strings.angryToneAlertTitle,
            strings.angryToneAlertMessage + "\n\nAcoustic frequency and volume analysis detected high aggressive stress levels.",
            Icons.Default.Hearing,
            WarningAmber
        )
        DetectionAlertType.POWER_BUTTON_EMERGENCY -> Quad(
            strings.powerAlertTitle,
            strings.powerAlertMessage + "\n\nIntentional rapid power button press sequence detected. Emergency SOS beacon active.",
            Icons.Default.PowerSettingsNew,
            BrightRed
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .border(2.dp, accentColor, RoundedCornerShape(24.dp))
            .testTag("detection_alert_dialog"),
        containerColor = SurfaceDark,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Text(
                text = message,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier.testTag("dismiss_alert_button")
            ) {
                Text("Acknowledge & Safe", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
