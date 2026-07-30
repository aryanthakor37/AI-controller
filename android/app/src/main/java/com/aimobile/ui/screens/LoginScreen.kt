package com.aimobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aimobile.ui.components.AuroraBackground
import com.aimobile.ui.components.CustomTextField
import com.aimobile.ui.components.PrimaryButton
import com.aimobile.ui.theme.*
import com.aimobile.ui.viewmodel.AuthState
import com.aimobile.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgot: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // ── Keep all existing state & logic ───────────────────────────────────────
    var email         by remember { mutableStateOf("") }
    var password      by remember { mutableStateOf("") }
    var errorMessage  by remember { mutableStateOf<String?>(null) }
    val authState     by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                errorMessage = null
                onLoginSuccess()
                authViewModel.resetState()
            }
            is AuthState.Error -> {
                errorMessage = (authState as AuthState.Error).message
            }
            else -> {}
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    AuroraBackground {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
            Spacer(Modifier.height(72.dp))

            // Logo
            Box(
                Modifier
                    .size(72.dp)
                    .background(
                        Brush.linearGradient(listOf(Primary, Secondary)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("AI", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(24.dp))
            Text("Welcome back", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text("Sign in to Agent.AI", color = TextSub, fontSize = 15.sp)
            Spacer(Modifier.height(40.dp))

            // Form card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                CustomTextField(
                    value = email, onValueChange = { email = it },
                    label = "Email Address", leadingIcon = Icons.Rounded.Email
                )
                Spacer(Modifier.height(16.dp))
                CustomTextField(
                    value = password, onValueChange = { password = it },
                    label = "Password",
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = Icons.Rounded.Lock
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        "Forgot Password?",
                        color = Primary, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onNavigateToForgot() }
                    )
                }

                // Animated error banner
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter   = slideInVertically { -it } + fadeIn(),
                    exit    = slideOutVertically { -it } + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(Danger.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Warning, contentDescription = null,
                            tint = Danger, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(errorMessage ?: "", color = Danger, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                PrimaryButton(
                    text      = "Sign In",
                    isLoading = authState is AuthState.Loading,
                    onClick   = {
                        errorMessage = null
                        authViewModel.login(email, password)
                    }
                )
            }

            Spacer(Modifier.height(28.dp))
            Row {
                Text("Don't have an account? ", color = TextSub, fontSize = 14.sp)
                Text(
                    "Sign up", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}
}
