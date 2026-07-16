package com.aimobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aimobile.ui.screens.*
import com.aimobile.ui.viewmodel.AuthViewModel
import com.aimobile.ui.viewmodel.MockViewModel
import com.aimobile.voice.viewmodel.VoiceViewModel
import com.aimobile.voice.ui.VoiceSettingsScreen
import com.aimobile.ui.screens.AccessibilityPermissionScreen
import com.aimobile.ui.viewmodel.CloudViewModel
import com.aimobile.ui.screens.DeviceManagerScreen
import com.aimobile.ui.screens.BackupRestoreScreen
import com.aimobile.ui.screens.AnalyticsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val mockViewModel: MockViewModel = hiltViewModel()

    // Determine start destination based on saved token
    val startDestination = if (authViewModel.isLoggedIn) "main" else "splash"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("splash") {
            SplashScreen(onNavigateNext = {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgot = { navController.navigate("forgot_password") },
                authViewModel = authViewModel
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onResetRequested = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("main") {
            val voiceViewModel: VoiceViewModel = hiltViewModel()
            MainScreen(
                viewModel = mockViewModel,
                authViewModel = authViewModel,
                onNavigateToVoiceSettings = { navController.navigate("voice_settings") },
                onNavigateToAccessibility = { navController.navigate("accessibility_guide") },
                onNavigateToDevices = { navController.navigate("devices") },
                onNavigateToBackup = { navController.navigate("backup") },
                onNavigateToAnalytics = { navController.navigate("analytics") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }

        composable("voice_settings") {
            val voiceViewModel: VoiceViewModel = hiltViewModel()
            VoiceSettingsScreen(
                viewModel = voiceViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("accessibility_guide") {
            AccessibilityPermissionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("devices") {
            val cloudViewModel: CloudViewModel = hiltViewModel()
            DeviceManagerScreen(
                viewModel = cloudViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("backup") {
            val cloudViewModel: CloudViewModel = hiltViewModel()
            BackupRestoreScreen(
                viewModel = cloudViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("analytics") {
            val cloudViewModel: CloudViewModel = hiltViewModel()
            AnalyticsScreen(
                viewModel = cloudViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
