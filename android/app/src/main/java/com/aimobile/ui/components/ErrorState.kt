package com.aimobile.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SignalWifiOff
import androidx.compose.material.icons.rounded.TimerOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.theme.*

enum class ErrorType {
    NETWORK,
    PERMISSION,
    TIMEOUT,
    SERVER
}

@Composable
fun ErrorCardBanner(
    message: String,
    type: ErrorType = ErrorType.SERVER,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (type) {
        ErrorType.NETWORK -> Icons.Rounded.SignalWifiOff
        ErrorType.PERMISSION -> Icons.Rounded.Security
        ErrorType.TIMEOUT -> Icons.Rounded.TimerOff
        ErrorType.SERVER -> Icons.Rounded.Warning
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Danger.copy(alpha = 0.12f))
            .border(1.dp, Danger.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Danger.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Danger,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = Danger,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }

            if (onRetry != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onRetry,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Retry",
                        tint = Danger
                    )
                }
            }
        }
    }
}
