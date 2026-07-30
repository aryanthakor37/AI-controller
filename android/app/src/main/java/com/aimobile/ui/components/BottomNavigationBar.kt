package com.aimobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aimobile.ui.theme.*

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home",    "dashboard", Icons.Filled.Home),
    BottomNavItem("AI Chat", "ai_chat",   Icons.Filled.Email),
    BottomNavItem("Voice",   "voice",     Icons.Filled.Mic),
    BottomNavItem("History", "history",   Icons.Filled.List),
    BottomNavItem("Settings","settings",  Icons.Filled.Settings)
)

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xB30F101A),
        contentColor   = TextPrimary,
        tonalElevation = 0.dp,
        modifier       = Modifier.background(Color(0xB30F101A))
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            val iconScale by animateFloatAsState(
                targetValue   = if (isSelected) 1.15f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessLow
                ),
                label = "nav_scale_${item.route}"
            )

            NavigationBarItem(
                selected  = isSelected,
                onClick   = { onNavigate(item.route) },
                icon      = {
                    Icon(
                        imageVector    = item.icon,
                        contentDescription = item.title,
                        modifier       = Modifier.scale(iconScale).size(22.dp)
                    )
                },
                label     = { Text(text = item.title) },
                colors    = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Color.White,
                    unselectedIconColor = TextSub,
                    selectedTextColor   = Primary,
                    unselectedTextColor = TextSub,
                    indicatorColor      = Primary.copy(alpha = 0.20f)
                )
            )
        }
    }
}
