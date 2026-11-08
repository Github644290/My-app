package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceSecurityLock
import com.example.model.LocalizedStrings
import com.example.ui.theme.BrightGreen
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
fun LoginScreen(
    securityLock: DeviceSecurityLock,
    strings: LocalizedStrings,
    errorMessage: String?,
    onLogin: (username: String, pass: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialUser = securityLock.lockedUsername ?: "peer_user_1"
    var username by remember { mutableStateOf(initialUser) }
    var password by remember { mutableStateOf("alpha_secure_99") }
    var passwordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Requirement:
    // "Login Screen: Must have a solid black background, with input text fields for the username and password rendered in bright red."
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SolidBlack)
            .testTag("login_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High-Tech P2P Brand Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .border(2.dp, BrightRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = BrightRed,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = strings.appTitle,
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                text = "SERVERLESS PEER-TO-PEER ENCLAVE",
                color = BrightRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Device IMEI Detection & 15-Day Security Policy Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (securityLock.isPeriodActive) WarningAmber else BrightRedDark, RoundedCornerShape(16.dp))
                    .testTag("imei_security_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = strings.deviceSecurityTitle,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${strings.imeiDetected}: ${securityLock.deviceImeiOrId}",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (securityLock.isPeriodActive && securityLock.lockedUsername != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(Icons.Default.Lock, null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Device Locked to: ${securityLock.lockedUsername}",
                                color = WarningAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Remaining Lock: ${securityLock.remainingFormatted} (until ${securityLock.expiryFormatted})",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = strings.lockPeriodNotice,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error display if account switching is blocked
            if (!errorMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrightRed.copy(alpha = 0.15f))
                        .border(1.dp, BrightRed, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .testTag("login_error_banner")
                ) {
                    Text(
                        text = errorMessage,
                        color = BrightRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 17.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Requirement:
            // Input text fields for the username and password rendered in bright red.
            // Bright red outline, bright red text, bright red cursor, bright red label!
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(strings.usernameLabel, color = BrightRed, fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = BrightRed
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BrightRed,
                    unfocusedTextColor = BrightRed,
                    focusedBorderColor = BrightRed,
                    unfocusedBorderColor = BrightRed.copy(alpha = 0.8f),
                    cursorColor = BrightRed,
                    focusedLabelColor = BrightRed,
                    unfocusedLabelColor = BrightRed,
                    focusedLeadingIconColor = BrightRed,
                    unfocusedLeadingIconColor = BrightRed,
                    focusedContainerColor = SolidBlack,
                    unfocusedContainerColor = SolidBlack
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("username_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field rendered in bright red
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(strings.passwordLabel, color = BrightRed, fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = BrightRed
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = BrightRed
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onLogin(username, password)
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BrightRed,
                    unfocusedTextColor = BrightRed,
                    focusedBorderColor = BrightRed,
                    unfocusedBorderColor = BrightRed.copy(alpha = 0.8f),
                    cursorColor = BrightRed,
                    focusedLabelColor = BrightRed,
                    unfocusedLabelColor = BrightRed,
                    focusedLeadingIconColor = BrightRed,
                    unfocusedLeadingIconColor = BrightRed,
                    focusedTrailingIconColor = BrightRed,
                    unfocusedTrailingIconColor = BrightRed,
                    focusedContainerColor = SolidBlack,
                    unfocusedContainerColor = SolidBlack
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onLogin(username, password)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrightRedDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("login_submit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.loginButton,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick demo test autofill shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rule: Both devices must login with identical credentials to pair.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
