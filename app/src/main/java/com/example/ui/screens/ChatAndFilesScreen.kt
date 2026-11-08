package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocalizedStrings
import com.example.model.MessageType
import com.example.model.P2PMessage
import com.example.ui.theme.BrightGreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SolidBlack
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhatsAppBubbleReceived
import com.example.ui.theme.WhatsAppBubbleSent
import com.example.ui.theme.WhatsAppChatBg
import com.example.ui.theme.WhatsAppGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAndFilesScreen(
    messages: List<P2PMessage>,
    strings: LocalizedStrings,
    peerUsername: String,
    onSendMessage: (String) -> Unit,
    onSendFile: (fileName: String, fileSize: String, mimeType: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhatsAppChatBg)
            .imePadding()
            .testTag("chat_files_screen")
    ) {
        // WhatsApp-style Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("chat_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(WhatsAppGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = WhatsAppGreen,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peerUsername.ifBlank { "Encrypted P2P Peer" },
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "End-to-End P2P Encrypted Socket",
                    color = BrightGreen,
                    fontSize = 11.sp
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = msg.isMine
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                )
                            )
                            .background(if (isMe) WhatsAppBubbleSent else WhatsAppBubbleReceived)
                            .border(
                                0.5.dp,
                                if (isMe) WhatsAppGreen.copy(alpha = 0.3f) else SurfaceBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            if (!isMe) {
                                Text(
                                    text = msg.senderUsername,
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }

                            if (msg.type == MessageType.FILE || msg.type == MessageType.IMAGE) {
                                // WhatsApp-style file card inside bubble
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SolidBlack.copy(alpha = 0.25f))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (msg.type == MessageType.IMAGE) Icons.Default.Image else Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        tint = WhatsAppGreen,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = msg.fileName ?: "Attachment",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = msg.fileSizeFormatted ?: "P2P Stream",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Downloaded",
                                        tint = BrightGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Text(
                                text = msg.content,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                lineHeight = 19.sp
                            )

                            Row(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = timeStr,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                                if (isMe) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Delivered",
                                        tint = CyberCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Paperclip for WhatsApp-style file sharing
            IconButton(
                onClick = { showAttachmentSheet = true },
                modifier = Modifier.testTag("attachment_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach file",
                    tint = WhatsAppGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text(strings.sendTextHint, color = TextMuted, fontSize = 13.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = WhatsAppGreen,
                    unfocusedBorderColor = SurfaceBorder,
                    cursorColor = WhatsAppGreen,
                    focusedContainerColor = SurfaceElevated,
                    unfocusedContainerColor = SurfaceElevated
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Green send button
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(WhatsAppGreen)
                    .testTag("send_message_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = SolidBlack,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // WhatsApp-style Attachment Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .testTag("attachment_bottom_sheet")
            ) {
                Text(
                    text = "WhatsApp-style P2P File Sharing",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOptionItem(
                        icon = Icons.Default.PictureAsPdf,
                        label = "Document",
                        color = Color(0xFF7F66FF)
                    ) {
                        onSendFile("Project_Proposal.pdf", "2.4 MB", "application/pdf")
                        showAttachmentSheet = false
                    }

                    AttachmentOptionItem(
                        icon = Icons.Default.PhotoCamera,
                        label = "Camera",
                        color = Color(0xFFFF2E93)
                    ) {
                        onSendFile("P2P_Snapshot.jpg", "3.8 MB", "image/jpeg")
                        showAttachmentSheet = false
                    }

                    AttachmentOptionItem(
                        icon = Icons.Default.Image,
                        label = "Gallery",
                        color = Color(0xFFC32AA3)
                    ) {
                        onSendFile("Encrypted_Photo.png", "1.9 MB", "image/png")
                        showAttachmentSheet = false
                    }

                    AttachmentOptionItem(
                        icon = Icons.Default.Audiotrack,
                        label = "Audio",
                        color = Color(0xFFFF9800)
                    ) {
                        onSendFile("Voice_Recording.aac", "850 KB", "audio/aac")
                        showAttachmentSheet = false
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AttachmentOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
    }
}
