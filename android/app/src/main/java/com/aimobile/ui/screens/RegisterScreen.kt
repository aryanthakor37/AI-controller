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
import androidx.compose.material.icons.rounded.Person
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
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // ── Keep all existing state & logic ───────────────────────────────────────
    var name         by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authState    by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                errorMessage = null
                onRegisterSuccess()
                authViewModel.resetState()
            }
            is AuthState.Error -> errorMessage = (authState as AuthState.Error).message
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
                            Brush.linearGradient(listOf(Secondary, Primary)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AI", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }

            Spacer(Modifier.height(24.dp))
            Text("Create Account", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text("Join Agent.AI today", color = TextSub, fontSize = 15.sp)
            Spacer(Modifier.height(40.dp))

            // Form card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                CustomTextField(
                    value = name, onValueChange = { name = it },
                    label = "Full Name", leadingIcon = Icons.Rounded.Person
                )
                Spacer(Modifier.height(16.dp))
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

                // Password strength bar
                if (password.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    val strength = when {
                        password.length >= 12 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 3
                        password.length >= 8 -> 2
                        else -> 1
                    }
                    val strengthColor = when (strength) {
                        3 -> Success
                        2 -> Accent
                        else -> Danger
                    }
                    val strengthLabel = when (strength) {
                        3 -> "Strong"
                        2 -> "Medium"
                        else -> "Weak"
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { idx ->
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .background(
                                        if (idx < strength) strengthColor else BorderColor,
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(strengthLabel, color = strengthColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
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
                    text      = "Create Account",
                    isLoading = authState is AuthState.Loading,
                    onClick   = {
                        errorMessage = null
                        authViewModel.register(name, email, password)
                    }
                )
            }

            Spacer(Modifier.height(28.dp))
            Row {
                Text("Already have an account? ", color = TextSub, fontSize = 14.sp)
                Text(
                    "Sign in", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}
}
