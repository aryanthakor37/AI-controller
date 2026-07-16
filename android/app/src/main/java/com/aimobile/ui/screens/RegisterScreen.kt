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
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsState()

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
                Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Join Agent.AI today", fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(32.dp))

                CustomTextField(value = name, onValueChange = { name = it }, label = "Full Name")
                Spacer(modifier = Modifier.height(16.dp))
                CustomTextField(value = email, onValueChange = { email = it }, label = "Email Address")
                Spacer(modifier = Modifier.height(16.dp))
                CustomTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    visualTransformation = PasswordVisualTransformation()
                )

                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(errorMessage!!, color = Color.Red.copy(alpha = 0.8f), fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = PrimaryBlue)
                } else {
                    PrimaryButton(text = "Sign Up", onClick = {
                        errorMessage = null
                        authViewModel.register(name, email, password)
                    })
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    Text("Already have an account? ", color = Color.White.copy(alpha = 0.6f))
                    Text(
                        "Sign in", color = PrimaryBlue, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}
