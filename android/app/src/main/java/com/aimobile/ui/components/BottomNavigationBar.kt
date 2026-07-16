package com.aimobile.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.aimobile.ui.theme.PrimaryBlue
import com.aimobile.ui.theme.SurfaceDark

import androidx.compose.material.icons.filled.Mic

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home", "dashboard", Icons.Filled.Home),
    BottomNavItem("AI Chat", "ai_chat", Icons.Filled.Email),
    BottomNavItem("Voice", "voice", Icons.Filled.Mic),
    BottomNavItem("History", "history", Icons.Filled.List),
    BottomNavItem("Settings", "settings", Icons.Filled.Settings)
)

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceDark,
        contentColor = Color.White
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    selectedTextColor = PrimaryBlue,
                    unselectedTextColor = Color.White.copy(alpha = 0.5f),
                    indicatorColor = PrimaryBlue.copy(alpha = 0.3f)
                )
            )
        }
    }
}
