package com.aimobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.RadioButtonChecked
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
    isRecordingMacro: Boolean = false,
    macroRecordedStepCount: Int = 0,
    onMicClick: () -> Unit,
    onSendText: (String) -> Unit,
    onRecordMacroClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
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

    // Dynamic Siri Glow Pulsing Wave Animations
    val infiniteTransition = rememberInfiniteTransition(label = "waveAnimation")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        if (!isExpanded) {
            // Collapsed Floating Glass Pill
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Background Glowing Siri Wave Aura when listening or thinking
                if (voiceState is VoiceState.Listening || voiceState is VoiceState.Thinking || isRecordingMacro) {
                    Canvas(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(pulseScale)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = if (isRecordingMacro) {
                                    listOf(Color(0xFFFF1744).copy(alpha = glowAlpha), Color.Transparent)
                                } else {
                                    listOf(Color(0xFF00E5FF).copy(alpha = glowAlpha), Color(0xFF7C4DFF).copy(alpha = 0.1f), Color.Transparent)
                                }
                            )
                        )
                    }
                }

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
                                colors = if (isRecordingMacro) {
                                    listOf(Color(0xFFD50000), Color(0xFFFF5252))
                                } else {
                                    listOf(Color(0xFF0D47A1), Color(0xFF00E5FF))
                                }
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Mic Trigger
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
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
                            .background(Color.White.copy(alpha = 0.35f))
                    )

                    // Expand / Keyboard Trigger
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                            .clickable {
                                toggleExpand(true)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Expand Panel",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else {
            // Expanded Frosted Obsidian Glass Card
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .wrapContentHeight()
                    .padding(6.dp)
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xF30B0F19) // Frosted Obsidian Dark Theme
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth()
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isRecordingMacro) Color(0xFFFF1744) else Color(0xFF00E5FF))
                            )
                            Text(
                                text = "AI Agent",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Macro REC Button
                            IconButton(
                                onClick = { onRecordMacroClick() },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RadioButtonChecked,
                                    contentDescription = "Record Macro",
                                    tint = if (isRecordingMacro) Color(0xFFFF1744) else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Minimize Button
                            IconButton(
                                onClick = { toggleExpand(false) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Minimize",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Voice & Execution Controls Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val micBg = when {
                                isRecordingMacro -> Color(0xFFFF1744)
                                voiceState is VoiceState.Listening -> Color(0xFFD50000)
                                else -> Color(0xFF00E5FF)
                            }
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(micBg)
                                    .clickable { onMicClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (voiceState is VoiceState.Listening) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Speech Trigger",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                val statusText = when {
                                    isRecordingMacro -> "REC ● $macroRecordedStepCount steps"
                                    voiceState is VoiceState.Idle -> "Tap mic or type command"
                                    voiceState is VoiceState.Listening -> "Listening..."
                                    voiceState is VoiceState.Thinking -> "Thinking..."
                                    voiceState is VoiceState.Processing -> "Processing..."
                                    voiceState is VoiceState.Executing -> "Executing action..."
                                    voiceState is VoiceState.Completed -> "Task Completed"
                                    voiceState is VoiceState.Failed -> "Error"
                                    else -> "Ready"
                                }
                                Text(
                                    text = statusText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRecordingMacro) Color(0xFFFF1744) else Color.White
                                )
                            }
                        }

                        // Stop/Cancel Button during active listening/executing
                        if (voiceState is VoiceState.Listening || voiceState is VoiceState.Executing || isRecordingMacro) {
                            IconButton(
                                onClick = { onCancelClick() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Cancel/Stop",
                                    tint = Color(0xFFFF1744),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Transcript Display
                    if (transcript.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "\"$transcript\"",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.07f))
                                .padding(8.dp)
                        )
                    }

                    // AI Response & Execution Progress Output
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Text Input Bar
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
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
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
                                .background(Color(0xFF00E5FF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Command",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
