package com.aimobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.aimobile.ui.components.BottomNavigationBar
import com.aimobile.ui.viewmodel.AuthState
import com.aimobile.ui.viewmodel.AuthViewModel
import com.aimobile.ui.viewmodel.MockViewModel
import com.aimobile.ui.viewmodel.CloudViewModel
import com.aimobile.voice.ui.VoiceScreen
import com.aimobile.voice.viewmodel.VoiceViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainScreen(
    viewModel: MockViewModel,
    authViewModel: AuthViewModel,
    onNavigateToVoiceSettings: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onLogout: () -> Unit
) {
    var currentRoute by remember { mutableStateOf("dashboard") }
    val voiceViewModel: VoiceViewModel = hiltViewModel()
    val cloudViewModel: CloudViewModel = hiltViewModel()

    // React to logout state
    val authState by authViewModel.authState.collectAsState()
    LaunchedEffect(authState) {
        if (authState is AuthState.LoggedOut) {
            onLogout()
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route }
            )
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = com.aimobile.R.drawable.bg_face),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Dark overlay (50% opacity) to keep text perfectly legible while making the art stand out
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0F1D).copy(alpha = 0.50f))
            )

            when (currentRoute) {
                "dashboard" -> DashboardScreen(viewModel = viewModel, modifier = modifier, onNavigateToProfile = { currentRoute = "profile" })
                "ai_chat" -> AIChatScreen(viewModel = viewModel, modifier = modifier)
                "voice" -> VoiceScreen(viewModel = voiceViewModel, onNavigateToSettings = onNavigateToVoiceSettings, modifier = modifier)
                "history" -> HistoryScreen(viewModel = cloudViewModel, modifier = modifier)
                "settings" -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToAccessibility = onNavigateToAccessibility,
                    onNavigateToDevices = onNavigateToDevices,
                    onNavigateToBackup = onNavigateToBackup,
                    onNavigateToAnalytics = onNavigateToAnalytics,
                    modifier = modifier
                )
                "profile" -> ProfileScreen(viewModel = viewModel, modifier = modifier, onLogout = { authViewModel.logout() })
            }
        }
    }
}

