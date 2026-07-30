package com.aimobile.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.theme.*

enum class DynamicIslandState {
    IDLE,
    LISTENING,
    PROCESSING,
    SUCCESS
}

@Composable
fun DynamicIslandPopup(
    state: DynamicIslandState,
    message: String = "AI Assistant Active",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (state == DynamicIslandState.IDLE) return

    val (bgGradient, icon, iconColor) = when (state) {
        DynamicIslandState.LISTENING -> Triple(
            listOf(Primary, Secondary),
            Icons.Rounded.Mic,
            Primary
        )
        DynamicIslandState.PROCESSING -> Triple(
            listOf(Secondary, Accent),
            Icons.Rounded.AutoAwesome,
            Accent
        )
        DynamicIslandState.SUCCESS -> Triple(
            listOf(Success, Primary),
            Icons.Rounded.CheckCircle,
            Success
        )
        DynamicIslandState.IDLE -> Triple(listOf(CardBg, CardBg), Icons.Rounded.AutoAwesome, TextSub)
    }

    val infinite = rememberInfiniteTransition(label = "island_pulse")
    val pulseScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "island_scale"
    )

    AnimatedVisibility(
        visible = state != DynamicIslandState.IDLE,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .scale(if (state == DynamicIslandState.PROCESSING || state == DynamicIslandState.LISTENING) pulseScale else 1f)
                    .shadow(16.dp, CircleShape, spotColor = iconColor.copy(alpha = 0.4f))
                    .clip(CircleShape)
                    .background(Color(0xFF0D0D12))
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(bgGradient),
                        shape = CircleShape
                    )
                    .clickable { onClick() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(iconColor.copy(alpha = 0.20f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = message,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (state == DynamicIslandState.PROCESSING || state == DynamicIslandState.LISTENING) {
                        Spacer(modifier = Modifier.width(10.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = iconColor,
                            strokeWidth = 1.8.dp
                        )
                    }
                }
            }
        }
    }
}
