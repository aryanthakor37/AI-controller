package com.aimobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.aimobile.ui.theme.BorderColor
import com.aimobile.ui.theme.CardBg

// ─── Shimmer Brush ─────────────────────────────────────────────────────────────
@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue  = -600f,
        targetValue   = 1400f,
        animationSpec = infiniteRepeatable(
            // 1400ms = slower sweep, cheaper GPU cost than 1000ms
            animation  = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )
    return remember(translateAnim) {
        Brush.linearGradient(
            colors = listOf(
                CardBg,
                BorderColor.copy(alpha = 0.9f),
                CardBg
            ),
            start = Offset(x = translateAnim, y = 0f),
            end   = Offset(x = translateAnim + 600f, y = 0f)
        )
    }
}

// ─── Generic Skeleton Box ──────────────────────────────────────────────────────
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 8
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(shimmerBrush())
    )
}

// ─── History Item Skeleton ─────────────────────────────────────────────────────
@Composable
fun HistoryItemSkeleton() {
    val brush = shimmerBrush()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(Modifier.fillMaxWidth(0.65f).height(15.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(0.45f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth(0.30f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            }
            Spacer(Modifier.width(12.dp))
            Box(Modifier.size(64.dp, 22.dp).clip(RoundedCornerShape(11.dp)).background(brush))
        }
    }
}
