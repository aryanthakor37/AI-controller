package com.aimobile.ui.screens

import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aimobile.api.ApiService
import com.aimobile.api.PairingRequest
import com.aimobile.utils.TokenManager
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun PairingScreen(onPaired: () -> Unit) {
    val context = LocalContext.current
    var pairingCode by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Build Retrofit manually here since PairingScreen runs before auth (no token yet)
    val apiService = remember {
        Retrofit.Builder()
            .baseUrl("https://initiative-equations-pix-kept.trycloudflare.com/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Link Device to Dashboard", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = pairingCode,
                onValueChange = { pairingCode = it },
                label = { Text("Enter 6-digit code") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                statusMessage = "Pairing..."
                coroutineScope.launch {
                    try {
                        val deviceId = Settings.Secure.getString(
                            context.contentResolver,
                            Settings.Secure.ANDROID_ID
                        )
                        val request = PairingRequest(
                            pairingCode = pairingCode,
                            deviceId = deviceId,
                            deviceName = Build.MODEL,
                            manufacturer = Build.MANUFACTURER,
                            model = Build.MODEL,
                            androidVersion = Build.VERSION.RELEASE
                        )
                        val response = apiService.linkDevice(request)
                        if (response.isSuccessful && response.body()?.token != null) {
                            statusMessage = "Paired successfully!"
                            val tokenManager = TokenManager(context)
                            // Save device token as the access token for socket auth
                            tokenManager.saveAccessToken(response.body()!!.token!!)
                            onPaired()
                        } else {
                            statusMessage = "Failed: ${response.body()?.message ?: "Invalid Code"}"
                        }
                    } catch (e: Exception) {
                        statusMessage = "Error connecting to server: ${e.message}"
                    }
                }
            }) {
                Text("Pair Device")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage,
                color = if (statusMessage.contains("success", true))
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}
