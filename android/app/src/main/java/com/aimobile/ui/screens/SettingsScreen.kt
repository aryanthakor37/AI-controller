package com.aimobile.ui.screens

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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text(text = "Settings", color = Color.White, fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingToggleRow(title = "Dark Theme", defaultChecked = true)
                Spacer(modifier = Modifier.height(16.dp))
                SettingToggleRow(title = "Push Notifications", defaultChecked = true)
                Spacer(modifier = Modifier.height(16.dp))
                SettingToggleRow(title = "Developer Mode", defaultChecked = false)
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

        val context = LocalContext.current
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
fun SettingToggleRow(title: String, defaultChecked: Boolean) {
    var checked by remember { mutableStateOf(defaultChecked) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryBlue
            )
        )
    }
}
