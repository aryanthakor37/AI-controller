package com.aimobile.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.aimobile.R
import com.aimobile.ui.theme.Background
import com.aimobile.ui.theme.Primary
import com.aimobile.ui.theme.Secondary
import com.aimobile.ui.theme.Accent
import kotlin.random.Random

private data class Particle(
    var x: Float,
    var y: Float,
    val radius: Float,
    val speedY: Float,
    val alpha: Float
)

@Composable
fun AuroraBackground(
    enabled: Boolean = true,
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val bgColor = animateColorAsState(
        targetValue = if (isDarkTheme) Background else Color(0xFF0F1229),
        animationSpec = tween(500),
        label = "bg_color"
    ).value

    val imgAlpha = animateFloatAsState(
        targetValue = if (isDarkTheme) 0.50f else 0.75f,
        animationSpec = tween(500),
        label = "img_alpha"
    ).value

    val particleColor = if (isDarkTheme) Color.White else Color(0xFF38BDF8)

    if (!enabled) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_ai_tech),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = imgAlpha,
                modifier = Modifier.fillMaxSize()
            )
            content()
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "aurora")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p2"
    )

    // Remember 18 lightweight particles
    val particles = remember {
        List(18) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 3f + 1.5f,
                speedY = Random.nextFloat() * 0.0008f + 0.0003f,
                alpha = Random.nextFloat() * 0.4f + 0.1f
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_ai_tech),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = imgAlpha,
            modifier = Modifier.fillMaxSize()
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw rotating Aurora Blobs
            val blob1Center = Offset(
                x = width * (0.2f + 0.3f * pulse1),
                y = height * (0.15f + 0.25f * pulse2)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Primary.copy(alpha = if (isDarkTheme) 0.18f else 0.30f), Color.Transparent),
                    center = blob1Center,
                    radius = width * 0.7f
                ),
                center = blob1Center,
                radius = width * 0.7f
            )

            val blob2Center = Offset(
                x = width * (0.8f - 0.3f * pulse2),
                y = height * (0.6f + 0.2f * pulse1)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Secondary.copy(alpha = if (isDarkTheme) 0.14f else 0.25f), Color.Transparent),
                    center = blob2Center,
                    radius = width * 0.65f
                ),
                center = blob2Center,
                radius = width * 0.65f
            )

            val blob3Center = Offset(
                x = width * (0.5f + 0.2f * pulse2),
                y = height * (0.85f - 0.25f * pulse1)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Accent.copy(alpha = if (isDarkTheme) 0.10f else 0.22f), Color.Transparent),
                    center = blob3Center,
                    radius = width * 0.60f
                ),
                center = blob3Center,
                radius = width * 0.60f
            )

            // 2. Draw Floating Particles
            particles.forEach { p ->
                p.y -= p.speedY
                if (p.y < 0f) p.y = 1f

                drawCircle(
                    color = particleColor.copy(alpha = p.alpha),
                    radius = p.radius,
                    center = Offset(p.x * width, p.y * height)
                )
            }
        }

        content()
    }
}
