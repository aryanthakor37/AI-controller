package com.aimobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.theme.*

enum class EmptyStateType {
    CHAT,
    HISTORY,
    DEVICES,
    NETWORK,
    SEARCH
}

@Composable
fun EmptyState(
    type: EmptyStateType,
    title: String? = null,
    subtitle: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionText: String? = null,
    modifier: Modifier = Modifier
) {
    val (icon, defaultTitle, defaultSubtitle, iconColor) = when (type) {
        EmptyStateType.CHAT -> Quadruple(
            Icons.Rounded.Forum,
            "Start a Conversation",
            "Ask AI to perform tasks, summarize info, or control your phone.",
            Primary
        )
        EmptyStateType.HISTORY -> Quadruple(
            Icons.Rounded.History,
            "No Commands Yet",
            "Your executed agent actions and history will appear here.",
            Secondary
        )
        EmptyStateType.DEVICES -> Quadruple(
            Icons.Rounded.Devices,
            "No Linked Devices",
            "Pair a device using your 6-digit code to start controlling it.",
            Accent
        )
        EmptyStateType.NETWORK -> Quadruple(
            Icons.Rounded.WifiOff,
            "Offline Mode",
            "Check your internet connection or server URL configuration.",
            Danger
        )
        EmptyStateType.SEARCH -> Quadruple(
            Icons.Rounded.SearchOff,
            "No Results Found",
            "Try searching with different keywords or clear filters.",
            TextSub
        )
    }

    val infinite = rememberInfiniteTransition(label = "empty_pulse")
    val scale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "empty_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(scale)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(iconColor.copy(alpha = 0.20f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title ?: defaultTitle,
                        tint = iconColor,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title ?: defaultTitle,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle ?: defaultSubtitle,
                color = TextSub,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )

            if (onActionClick != null && actionText != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = iconColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = actionText, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
