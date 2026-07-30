package com.aimobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aimobile.ui.theme.BorderColor
import com.aimobile.ui.theme.CardBg
import com.aimobile.ui.theme.Primary
import com.aimobile.ui.theme.Secondary

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    glowColor: Color = Primary,
    showGradientBorder: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation  = 20.dp,
                shape      = RoundedCornerShape(cornerRadius),
                clip       = false,
                ambientColor = Color.Black.copy(alpha = 0.8f),
                spotColor    = glowColor.copy(alpha = 0.20f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(CardBg)
            .then(
                if (showGradientBorder) {
                    Modifier.border(
                        width  = 1.dp,
                        brush  = Brush.linearGradient(
                            listOf(
                                Primary.copy(alpha = 0.55f),
                                Secondary.copy(alpha = 0.30f),
                                Color.Transparent
                            )
                        ),
                        shape  = RoundedCornerShape(cornerRadius)
                    )
                } else {
                    Modifier.border(1.dp, BorderColor, RoundedCornerShape(cornerRadius))
                }
            )
    ) {
        content()
    }
}
