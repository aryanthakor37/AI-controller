package com.aimobile.ui.screens

import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.api.ApiService
import com.aimobile.api.PairingRequest
import com.aimobile.ui.components.PrimaryButton
import com.aimobile.ui.theme.*
import com.aimobile.utils.TokenManager
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun PairingScreen(onPaired: () -> Unit) {
    // ── Keep all existing logic ───────────────────────────────────────────────
    val context      = LocalContext.current
    var pairingCode  by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val apiService = remember {
        val tokenManager = TokenManager(context)
        val baseUrl = tokenManager.getServerUrl().let {
            if (it.endsWith("/")) it else "$it/"
        }
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Pulse animation for the key icon ring
    val infinite = rememberInfiniteTransition(label = "pair_pulse")
    val pulseAlpha by infinite.animateFloat(
        initialValue = 0.15f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pair_alpha"
    )

    val isSuccess = statusMessage.contains("success", true)
    val isError   = statusMessage.isNotEmpty() && !isSuccess
    val isPairing = statusMessage == "Pairing..."

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.fillMaxWidth().height(300.dp).align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.06f), Color.Transparent)))
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon with pulsing ring
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Brush.radialGradient(listOf(Primary.copy(alpha = pulseAlpha), Color.Transparent)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(76.dp)
                        .background(Brush.linearGradient(listOf(Primary, Secondary)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.Key,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
            Text("Link Device", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Enter the 6-digit code from your Agent.AI dashboard",
                color = TextSub, fontSize = 14.sp,
                textAlign = TextAlign.Center, lineHeight = 20.sp
            )
            Spacer(Modifier.height(36.dp))

            // Code input card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(24.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                OutlinedTextField(
                    value         = pairingCode,
                    onValueChange = { if (it.length <= 8) pairingCode = it },
                    label         = { Text("Pairing Code") },
                    placeholder   = { Text("e.g. A1B2C3") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(14.dp),
                    leadingIcon   = { Icon(Icons.Rounded.Key, null) },
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedBorderColor      = Primary,
                        unfocusedBorderColor    = BorderColor,
                        focusedLabelColor       = Primary,
                        unfocusedLabelColor     = TextSub,
                        cursorColor             = Primary,
                        focusedTextColor        = TextPrimary,
                        unfocusedTextColor      = TextPrimary
                    )
                )

                // Status message
                AnimatedVisibility(
                    visible = statusMessage.isNotEmpty(),
                    enter   = slideInVertically { -it } + fadeIn(),
                    exit    = slideOutVertically { -it } + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(
                                when {
                                    isSuccess -> Success.copy(alpha = 0.10f)
                                    isError   -> Danger.copy(alpha = 0.10f)
                                    else      -> Primary.copy(alpha = 0.08f)
                                },
                                RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isError) Icon(Icons.Rounded.Warning, null, tint = Danger, modifier = Modifier.size(15.dp))
                        if (isSuccess) Icon(Icons.Rounded.CheckCircle, null, tint = Success, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            statusMessage,
                            color = when { isSuccess -> Success; isError -> Danger; else -> Primary },
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                PrimaryButton(
                    text      = "Pair Device",
                    isLoading = isPairing,
                    onClick   = {
                        statusMessage = "Pairing..."
                        coroutineScope.launch {
                            try {
                                val deviceId = Settings.Secure.getString(
                                    context.contentResolver, Settings.Secure.ANDROID_ID
                                )
                                val request = PairingRequest(
                                    pairingCode  = pairingCode,
                                    deviceId     = deviceId,
                                    deviceName   = Build.MODEL,
                                    manufacturer = Build.MANUFACTURER,
                                    model        = Build.MODEL,
                                    androidVersion = Build.VERSION.RELEASE
                                )
                                val response = apiService.linkDevice(request)
                                if (response.isSuccessful && response.body()?.token != null) {
                                    statusMessage = "Paired successfully!"
                                    val tokenManager = TokenManager(context)
                                    tokenManager.saveAccessToken(response.body()!!.token!!)
                                    onPaired()
                                } else {
                                    statusMessage = "Failed: ${response.body()?.message ?: "Invalid Code"}"
                                }
                            } catch (e: Exception) {
                                statusMessage = "Error connecting to server: ${e.message}"
                            }
                        }
                    }
                )
            }
        }
    }
}
