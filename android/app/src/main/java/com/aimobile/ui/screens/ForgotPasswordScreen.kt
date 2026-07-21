package com.aimobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aimobile.ui.components.CustomTextField
import com.aimobile.ui.components.GlassCard
import com.aimobile.ui.components.PrimaryButton
import com.aimobile.ui.theme.DarkBackground
import com.aimobile.ui.theme.PrimaryBlue
import com.aimobile.ui.viewmodel.AuthState
import com.aimobile.ui.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.ForgotPasswordSuccess -> {
                statusMessage = (authState as AuthState.ForgotPasswordSuccess).message
                isSuccess = true
                authViewModel.resetState()
            }
            is AuthState.Error -> {
                statusMessage = (authState as AuthState.Error).message
                isSuccess = false
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reset Password",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter your email to receive a reset link.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(32.dp))

                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address"
                )

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = statusMessage!!,
                        color = if (isSuccess) Color.Green.copy(alpha = 0.8f) else Color.Red.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = PrimaryBlue)
                } else {
                    PrimaryButton(text = "Send Link", onClick = {
                        statusMessage = null
                        authViewModel.forgotPassword(email)
                    })
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Back to Login",
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { 
                        authViewModel.resetState()
                        onNavigateBack() 
                    }
                )
            }
        }
    }
}
