package com.aimobile.voice.ui

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.components.GlassCard
import com.aimobile.ui.theme.*
import com.aimobile.voice.ui.components.MicButton
import com.aimobile.voice.viewmodel.VoiceState
import com.aimobile.voice.viewmodel.VoiceViewModel
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    viewModel: VoiceViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.voiceState.collectAsState()
    val transcript by viewModel.transcript.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.startListening()
    }

    LaunchedEffect(key1 = voiceState) {
        if (voiceState is VoiceState.PermissionDenied) {
            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    // Trigger haptic feedback function
    val triggerHaptic = {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(VibratorManager::class.java)
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Vibrator::class.java)
                vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (e: Exception) {
            // Ignore if vibration unsupported
        }
    }

    // Audio Visualizer wave phase animation
    val infiniteTransition = rememberInfiniteTransition(label = "voice_vis")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val isSpeaking = voiceState is VoiceState.Executing || voiceState is VoiceState.Completed

    val stateColor = when (voiceState) {
        is VoiceState.Failed -> Danger
        is VoiceState.Completed -> Success
        is VoiceState.Listening -> Primary
        is VoiceState.Processing,
        is VoiceState.Thinking,
        is VoiceState.Executing -> Accent
        else -> TextSub
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top row ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Voice Assistant", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg)
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = "Voice Settings", tint = TextSub)
            }
        }

        // ── Center visualizer & Audio Waves Canvas ────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Multi-ring visualizer when listening or speaking
                if (isListening || isSpeaking) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxRadius = size.width / 2f

                        // Draw sine wave rings
                        val barCount = 36
                        for (i in 0 until barCount) {
                            val angle = (i * 360.0 / barCount) * (Math.PI / 180.0)
                            val amplitude = if (isListening) 24f else 16f
                            val wave = (sin(angle * 3 + phase) * amplitude).toFloat()
                            val r = (maxRadius * 0.55f) + wave

                            val startX = (center.x + (maxRadius * 0.42f) * kotlin.math.cos(angle)).toFloat()
                            val startY = (center.y + (maxRadius * 0.42f) * sin(angle)).toFloat()
                            val endX = (center.x + r * kotlin.math.cos(angle)).toFloat()
                            val endY = (center.y + r * sin(angle)).toFloat()

                            drawLine(
                                color = if (isListening) Primary.copy(alpha = 0.7f) else Accent.copy(alpha = 0.7f),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 3.5.dp.toPx()
                            )
                        }
                    }
                }

                MicButton(
                    isListening = isListening,
                    onClick = {
                        triggerHaptic()
                        if (isListening) viewModel.stopListening()
                        else viewModel.startListening()
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            // State Badge Pill
            Box(
                Modifier
                    .background(stateColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .border(1.dp, stateColor.copy(alpha = 0.30f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = null,
                        tint = stateColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when (voiceState) {
                            is VoiceState.Idle -> "Tap mic to speak"
                            is VoiceState.Listening -> "Listening…"
                            is VoiceState.Processing -> "Processing audio…"
                            is VoiceState.Thinking -> "AI Thinking…"
                            is VoiceState.Executing -> "Executing action…"
                            is VoiceState.Completed -> "Task Completed ✓"
                            is VoiceState.Failed -> "Command Failed"
                            is VoiceState.PermissionDenied -> "Mic Permission Required"
                            is VoiceState.Offline -> "Offline mode"
                        },
                        color = stateColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ── Lower response & details card ─────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = transcript.isNotBlank(),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("You said:", color = TextSub, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text("\"$transcript\"", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            AnimatedVisibility(
                visible = aiResponse.isNotBlank(),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    showGradientBorder = true
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("AI Response:", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text(aiResponse, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Controls row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (voiceState is VoiceState.Failed || voiceState is VoiceState.Completed) {
                    Button(
                        onClick = { triggerHaptic(); viewModel.retry() },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Retry", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Retry", fontWeight = FontWeight.SemiBold)
                    }
                }

                if (voiceState != VoiceState.Idle) {
                    OutlinedButton(
                        onClick = { triggerHaptic(); viewModel.cancel() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                        shape = RoundedCornerShape(14.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
