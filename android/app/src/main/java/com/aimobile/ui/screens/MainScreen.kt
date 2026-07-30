package com.aimobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.aimobile.ui.components.AuroraBackground
import com.aimobile.ui.components.BottomNavigationBar
import com.aimobile.ui.theme.Background
import com.aimobile.ui.viewmodel.AuthState
import com.aimobile.ui.viewmodel.AuthViewModel
import com.aimobile.ui.viewmodel.CloudViewModel
import com.aimobile.ui.viewmodel.MockViewModel
import com.aimobile.voice.ui.VoiceScreen
import com.aimobile.voice.viewmodel.VoiceViewModel

@Composable
fun MainScreen(
    onNavigateToVoiceSettings: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onLogout: () -> Unit,
    viewModel: MockViewModel = hiltViewModel(),
    cloudViewModel: CloudViewModel = hiltViewModel(),
    voiceViewModel: VoiceViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    automationViewModel: com.aimobile.ui.viewmodel.AutomationViewModel = hiltViewModel()
) {
    var currentRoute by remember { mutableStateOf("dashboard") }
    var animationsEnabled by remember { mutableStateOf(true) }
    var darkThemeEnabled by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route }
            )
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)

        AuroraBackground(enabled = animationsEnabled, isDarkTheme = darkThemeEnabled) {
            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = {
                    fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) togetherWith
                    fadeOut(animationSpec = androidx.compose.animation.core.tween(180))
                },
                label = "screen_transition"
            ) { route ->
                when (route) {
                    "dashboard" -> DashboardScreen(
                        viewModel = viewModel,
                        modifier = modifier,
                        onNavigateToProfile = { currentRoute = "profile" }
                    )
                    "ai_chat" -> AIChatScreen(
                        viewModel = viewModel,
                        modifier = modifier
                    )
                    "voice" -> VoiceScreen(
                        viewModel = voiceViewModel,
                        onNavigateToSettings = onNavigateToVoiceSettings,
                        modifier = modifier
                    )
                    "history" -> HistoryScreen(
                        viewModel = cloudViewModel,
                        modifier = modifier
                    )
                    "settings" -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAccessibility = onNavigateToAccessibility,
                        onNavigateToDevices = onNavigateToDevices,
                        onNavigateToBackup = onNavigateToBackup,
                        onNavigateToAnalytics = onNavigateToAnalytics,
                        modifier = modifier,
                        animationsEnabled = animationsEnabled,
                        onToggleAnimations = { animationsEnabled = it },
                        darkThemeEnabled = darkThemeEnabled,
                        onToggleDarkTheme = { darkThemeEnabled = it },
                        onNavigateToAutomation = { currentRoute = "automation" }
                    )
                    "automation" -> com.aimobile.ui.screens.AutomationScreen(
                        viewModel = automationViewModel,
                        onNavigateBack = { currentRoute = "settings" },
                        modifier = modifier
                    )
                    "profile" -> ProfileScreen(
                        viewModel = viewModel,
                        modifier = modifier,
                        onLogout = { authViewModel.logout() }
                    )
                }
            }
        }
    }
}
