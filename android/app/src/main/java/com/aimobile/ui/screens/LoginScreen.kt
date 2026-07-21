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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgot: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var showOtpInput by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsState()

    // React to state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                errorMessage = null
                onLoginSuccess()
                authViewModel.resetState()
            }
            is AuthState.Error -> {
                val errorMsg = (authState as AuthState.Error).message
                errorMessage = errorMsg
                if (errorMsg.contains("verify your email", ignoreCase = true)) {
                    showOtpInput = true
                }
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
                if (showOtpInput) {
                    Text("Verify Email", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Verification code sent to $email", fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(32.dp))

                    CustomTextField(value = otpCode, onValueChange = { otpCode = it }, label = "Verification Code")
                    
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(errorMessage!!, color = Color.Red.copy(alpha = 0.8f), fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    } else {
                        PrimaryButton(text = "Verify & Sign In", onClick = {
                            errorMessage = null
                            authViewModel.verifyEmail(email, otpCode)
                        })
                    }
                } else {
                    Text("Sign In", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Welcome back to Agent.AI", fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(32.dp))

                    CustomTextField(value = email, onValueChange = { email = it }, label = "Email Address")
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(
                            "Forgot Password?",
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { onNavigateToForgot() }
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(errorMessage!!, color = Color.Red.copy(alpha = 0.8f), fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    } else {
                        PrimaryButton(text = "Log In", onClick = {
                            errorMessage = null
                            authViewModel.login(email, password)
                        })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    Text("Don't have an account? ", color = Color.White.copy(alpha = 0.6f))
                    Text(
                        "Sign up", color = PrimaryBlue, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}
