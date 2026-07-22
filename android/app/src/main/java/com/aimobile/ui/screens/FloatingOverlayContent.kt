package com.aimobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.voice.viewmodel.VoiceState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingOverlayContent(
    voiceState: VoiceState,
    transcript: String,
    aiResponse: String,
    onMicClick: () -> Unit,
    onSendText: (String) -> Unit,
    onDrag: (Float, Float) -> Unit,
    onCloseClick: () -> Unit,
    onExpandToggle: (Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val toggleExpand: (Boolean) -> Unit = { expanded ->
        isExpanded = expanded
        onExpandToggle(expanded)
    }
    var textInput by remember { mutableStateOf("") }
    
    // Animation for mic pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        if (!isExpanded) {
            // Floating Pill containing Mic and Keyboard triggers
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(48.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E88E5), // Premium Blue
                                Color(0xFF00E5FF)  // Accent Cyan
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Mic Icon (starts listening and expands panel to show speech outcome)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .scale(if (voiceState == VoiceState.Listening) pulseScale else 1.0f)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable {
                            onMicClick()
                            toggleExpand(true)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mic Trigger",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(Color.White.copy(alpha = 0.3f))
                )

                // Keyboard Icon (expands command panel to type text directly)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable {
                            toggleExpand(true)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Text Trigger",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            // Expanded Glassmorphic Sidebar Card
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .wrapContentHeight()
                    .padding(8.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xEB111827) // Dark obsidian gray with 92% alpha
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth()
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Mobile Control",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = { toggleExpand(false) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Minimize",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    // Voice controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Mic icon toggle
                        val micBg = if (voiceState == VoiceState.Listening) Color(0xFFE53935) else Color(0xFF1E88E5)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(micBg)
                                .clickable { onMicClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (voiceState == VoiceState.Listening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Speech Trigger",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        val statusText = when (voiceState) {
                            is VoiceState.Idle -> "Tap mic to speak"
                            is VoiceState.Listening -> "Listening..."
                            is VoiceState.Thinking -> "Thinking..."
                            is VoiceState.Processing -> "Processing..."
                            is VoiceState.Executing -> "Running action..."
                            is VoiceState.Completed -> "Action executed"
                            is VoiceState.Failed -> "Error occurred"
                            else -> "Ready"
                        }
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (voiceState == VoiceState.Listening) Color(0xFFE53935) else Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Transcript card layout
                    if (transcript.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "\"$transcript\"",
                            fontSize = 13.sp,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        )
                    }

                    // Response logs
                    if (aiResponse.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = aiResponse,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Text panel input box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Command...", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f)) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            textStyle = TextStyle(fontSize = 12.sp, color = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedContainerColor = Color.White.copy(alpha = 0.04f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                            )
                        )
                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    onSendText(textInput)
                                    textInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF1E88E5), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Command",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
