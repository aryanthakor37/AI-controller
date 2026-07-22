package com.aimobile.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.components.GlassCard
import com.aimobile.ui.theme.DarkBackground
import com.aimobile.ui.theme.PrimaryBlue
import com.aimobile.ui.viewmodel.MockViewModel
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun SettingsScreen(
    viewModel: MockViewModel,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var darkTheme by remember { mutableStateOf(true) }
    var pushNotifications by remember { mutableStateOf(true) }
    var developerMode by remember { mutableStateOf(false) }
    var overlayEnabled by remember { mutableStateOf(viewModel.isOverlayEnabled()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text(text = "Settings", color = Color.White, fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingToggleRow(title = "Dark Theme", checked = darkTheme, onCheckedChange = { darkTheme = it })
                Spacer(modifier = Modifier.height(16.dp))
                SettingToggleRow(title = "Push Notifications", checked = pushNotifications, onCheckedChange = { pushNotifications = it })
                Spacer(modifier = Modifier.height(16.dp))
                SettingToggleRow(title = "Developer Mode", checked = developerMode, onCheckedChange = { developerMode = it })
                Spacer(modifier = Modifier.height(16.dp))
                SettingToggleRow(
                    title = "Floating Controls Overlay",
                    checked = overlayEnabled,
                    onCheckedChange = { isChecked ->
                        overlayEnabled = isChecked
                        viewModel.saveOverlayEnabled(isChecked)
                        
                        val overlayIntent = Intent(context, com.aimobile.services.FloatingOverlayService::class.java)
                        if (isChecked) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && 
                                !android.provider.Settings.canDrawOverlays(context)) {
                                Toast.makeText(context, "Please grant Overlay permission to enable", Toast.LENGTH_LONG).show()
                                try {
                                    val intent = Intent(
                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        android.net.Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.util.Log.e("SettingsScreen", "Failed to start overlay settings: ${e.message}")
                                }
                            } else {
                                try {
                                    context.startService(overlayIntent)
                                } catch (e: Exception) {
                                    android.util.Log.e("SettingsScreen", "Failed to start FloatingOverlayService: ${e.message}")
                                }
                            }
                        } else {
                            try {
                                context.stopService(overlayIntent)
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Connection Settings",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        var serverUrl by remember { mutableStateOf(viewModel.getServerUrl()) }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server URL", color = Color.Gray) },
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                com.aimobile.ui.components.PrimaryButton(
                    text = "Save Server URL",
                    onClick = {
                        viewModel.saveServerUrl(serverUrl)
                        Toast.makeText(context, "Server URL saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Accessibility config button
        com.aimobile.ui.components.PrimaryButton(
            text = "Configure Accessibility Automation",
            onClick = onNavigateToAccessibility,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Multi-device button
        com.aimobile.ui.components.PrimaryButton(
            text = "Multi-Device Manager",
            onClick = onNavigateToDevices,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Backup Settings button
        com.aimobile.ui.components.PrimaryButton(
            text = "Cloud Backup & Restore",
            onClick = onNavigateToBackup,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Usage Analytics stats button
        com.aimobile.ui.components.PrimaryButton(
            text = "Agent Performance Stats",
            onClick = onNavigateToAnalytics,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SettingToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryBlue
            )
        )
    }
}
