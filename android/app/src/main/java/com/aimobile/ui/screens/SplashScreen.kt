package com.aimobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateNext: () -> Unit) {
    // ── Keep existing navigation logic ────────────────────────────────────────
    LaunchedEffect(key1 = true) {
        delay(1200L)  // Reduced from 2s for faster start
        onNavigateNext()
    }

    // ── Pulse animation ───────────────────────────────────────────────────────
    val infinite = rememberInfiniteTransition(label = "splash_pulse")
    val pulseScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue  = 1.14f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse_scale"
    )
    val ringAlpha by infinite.animateFloat(
        initialValue = 0.10f,
        targetValue  = 0.30f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "ring_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Outer pulsing glow ring
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(pulseScale)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(Primary.copy(alpha = ringAlpha), Color.Transparent)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Inner gradient circle — logo
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(Primary, Secondary)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text         = "AI",
                        color        = Color.White,
                        fontSize     = 26.sp,
                        fontWeight   = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text          = "Agent.AI",
                color         = TextPrimary,
                fontSize      = 30.sp,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = "Your AI-Powered Phone Agent",
                color      = TextSub,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
