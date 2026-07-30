package com.aimobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.aimobile.ui.theme.Accent
import com.aimobile.ui.theme.Primary
import com.aimobile.ui.theme.Secondary
import com.aimobile.ui.theme.Success
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var alpha: Float
)

@Composable
fun CelebrationAnimation(
    trigger: Boolean,
    onFinished: () -> Unit = {}
) {
    if (!trigger) return

    val progress = remember { Animatable(0f) }

    val particles = remember(trigger) {
        val colors = listOf(Primary, Secondary, Accent, Success, Color(0xFFFFD700))
        List(40) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = Random.nextFloat() * 12f + 4f
            ConfettiParticle(
                x = 0.5f,
                y = 0.4f,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 6f,
                color = colors.random(),
                size = Random.nextFloat() * 8f + 4f,
                alpha = 1f
            )
        }
    }

    LaunchedEffect(trigger) {
        if (trigger) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(1200, easing = FastOutSlowInEasing)
            )
            onFinished()
        }
    }

    if (progress.value < 1f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val currentProgress = progress.value

            particles.forEach { p ->
                val px = width * p.x + p.vx * currentProgress * 25f
                val py = height * p.y + (p.vy * currentProgress + 15f * currentProgress * currentProgress) * 20f
                val currentAlpha = (1f - currentProgress).coerceIn(0f, 1f)

                drawCircle(
                    color = p.color.copy(alpha = currentAlpha),
                    radius = p.size * (1f - currentProgress * 0.3f),
                    center = Offset(px, py)
                )
            }
        }
    }
}
