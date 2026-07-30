package com.aimobile.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.components.PrimaryButton
import com.aimobile.ui.theme.*
import com.aimobile.ui.viewmodel.MockViewModel

@Composable
fun SettingsScreen(
    viewModel: MockViewModel,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true,
    onToggleAnimations: (Boolean) -> Unit = {},
    darkThemeEnabled: Boolean = true,
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onNavigateToAutomation: () -> Unit = {}
) {
    val context = LocalContext.current
    var pushNotifications by remember { mutableStateOf(true) }
    var developerMode by remember { mutableStateOf(false) }
    var overlayEnabled by remember { mutableStateOf(viewModel.isOverlayEnabled()) }
    var serverUrl by remember { mutableStateOf(viewModel.getServerUrl()) }
    var selectedLanguage by remember { mutableStateOf("English (US)") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text("Settings", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(24.dp))

        // ── Visual & Animation Settings ───────────────────────────────────────
        SettingsSectionHeader("Appearance & Motion")
        Spacer(Modifier.height(8.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBg)
        ) {
            SettingToggleRow(
                title = "Aurora Animations",
                subtitle = "Enable dynamic liquid backgrounds & floating particles",
                icon = Icons.Rounded.AutoAwesome,
                checked = animationsEnabled,
                onCheckedChange = onToggleAnimations
            )
            SettingsDivider()
            SettingToggleRow(
                title = "Dark Theme",
                subtitle = if (darkThemeEnabled) "Deep Obsidian Glass Mode" else "Cyber Neon Light Mode",
                icon = Icons.Rounded.DarkMode,
                checked = darkThemeEnabled,
                onCheckedChange = onToggleDarkTheme
            )
            SettingsDivider()
            SettingsNavRow(
                title = "Automations & Routines",
                subtitle = "Configure multi-action scheduled routines",
                icon = Icons.Rounded.Bolt,
                onClick = onNavigateToAutomation
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Agent Preferences Section ─────────────────────────────────────────
        SettingsSectionHeader("Agent Preferences")
        Spacer(Modifier.height(8.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBg)
        ) {
            SettingToggleRow(
                title = "Push Notifications",
                subtitle = "Alerts on action completion & errors",
                icon = Icons.Rounded.Notifications,
                checked = pushNotifications,
                onCheckedChange = { pushNotifications = it }
            )
            SettingsDivider()
            SettingToggleRow(
                title = "Floating Control Bar",
                subtitle = "Overlay quick controls on other apps",
                icon = Icons.Rounded.Layers,
                checked = overlayEnabled,
                onCheckedChange = { isChecked ->
                    overlayEnabled = isChecked
                    viewModel.saveOverlayEnabled(isChecked)
                    val overlayIntent = Intent(context, com.aimobile.services.FloatingOverlayService::class.java)
                    if (isChecked) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                            !android.provider.Settings.canDrawOverlays(context)) {
                            Toast.makeText(context, "Please grant Overlay permission", Toast.LENGTH_LONG).show()
                            try {
                                val intent = Intent(
                                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    android.net.Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) { android.util.Log.e("SettingsScreen", "Failed: ${e.message}") }
                        } else {
                            try { context.startService(overlayIntent) }
                            catch (e: Exception) { android.util.Log.e("SettingsScreen", "Failed: ${e.message}") }
                        }
                    } else {
                        try { context.stopService(overlayIntent) } catch (e: Exception) {}
                    }
                }
            )
            SettingsDivider()
            SettingToggleRow(
                title = "Developer Mode",
                subtitle = "Show verbose logs and debug payloads",
                icon = Icons.Rounded.Code,
                checked = developerMode,
                onCheckedChange = { developerMode = it }
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Connection Section ────────────────────────────────────────────────
        SettingsSectionHeader("Backend Connection")
        Spacer(Modifier.height(8.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBg)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Server Base URL") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Cloud, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Background,
                    unfocusedContainerColor = Background,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = Primary,
                    unfocusedLabelColor = TextSub,
                    cursorColor = Primary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = "Save Server URL",
                onClick = {
                    viewModel.saveServerUrl(serverUrl)
                    Toast.makeText(context, "Server URL saved!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Advanced Features & Tools ─────────────────────────────────────────
        SettingsSectionHeader("Tools & Privacy")
        Spacer(Modifier.height(8.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBg)
        ) {
            SettingsNavRow(
                title = "Accessibility Automation",
                subtitle = "Configure WhatsApp/YouTube automations",
                icon = Icons.Rounded.Accessibility,
                onClick = onNavigateToAccessibility
            )
            SettingsDivider()
            SettingsNavRow(
                title = "Multi-Device Manager",
                subtitle = "Pair & view linked mobile devices",
                icon = Icons.Rounded.Devices,
                onClick = onNavigateToDevices
            )
            SettingsDivider()
            SettingsNavRow(
                title = "Cloud Backup & Restore",
                subtitle = "Export or import your configurations",
                icon = Icons.Rounded.Backup,
                onClick = onNavigateToBackup
            )
            SettingsDivider()
            SettingsNavRow(
                title = "Agent Performance Stats",
                subtitle = "Analytics & response metrics",
                icon = Icons.Rounded.Analytics,
                onClick = onNavigateToAnalytics
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── About & Version Section ───────────────────────────────────────────
        SettingsSectionHeader("System & About")
        Spacer(Modifier.height(8.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBg)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("App Version", color = TextSub, fontSize = 13.sp)
                Text("v2.4.0 (Production)", color = Primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AI Engine", color = TextSub, fontSize = 13.sp)
                Text("Google Gemini Pro", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = TextSub,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsDivider() {
    Divider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String = "",
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(Primary.copy(alpha = 0.10f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, color = TextSub, fontSize = 12.sp)
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary,
                uncheckedThumbColor = TextSub,
                uncheckedTrackColor = BorderColor
            )
        )
    }
}

@Composable
fun SettingsNavRow(
    title: String,
    subtitle: String = "",
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(Primary.copy(alpha = 0.10f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, color = TextSub, fontSize = 12.sp)
                }
            }
        }
        Icon(
            Icons.Rounded.ChevronRight, contentDescription = null,
            tint = TextSub, modifier = Modifier.size(20.dp)
        )
    }
}
