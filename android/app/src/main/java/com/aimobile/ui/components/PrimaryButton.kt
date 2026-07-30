package com.aimobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.theme.Primary
import com.aimobile.ui.theme.Secondary

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    var pressed by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "btn_scale"
    )
    val alpha = if (enabled) 1f else 0.50f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation    = if (pressed) 4.dp else 14.dp,
                shape        = RoundedCornerShape(16.dp),
                ambientColor = Primary.copy(alpha = 0.25f),
                spotColor    = Primary.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Primary.copy(alpha = alpha),
                        Secondary.copy(alpha = alpha)
                    )
                )
            )
            .pointerInput(enabled, isLoading) {
                if (!enabled || isLoading) return@pointerInput
                detectTapGestures(
                    onPress = {
                        pressed = true
                        try { com.aimobile.utils.HapticHelper.performHaptic(context, com.aimobile.utils.HapticType.MEDIUM) } catch (e: Exception) {}
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    }
                )
            }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(22.dp),
                color       = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text         = text,
                color        = Color.White.copy(alpha = alpha),
                fontWeight   = FontWeight.SemiBold,
                fontSize     = 16.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}
