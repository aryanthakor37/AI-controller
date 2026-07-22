package com.aimobile.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.components.GlassCard
import com.aimobile.ui.theme.AccentPurple
import com.aimobile.ui.theme.DarkBackground
import com.aimobile.ui.theme.PrimaryBlue
import com.aimobile.ui.theme.SurfaceDark
import com.aimobile.ui.viewmodel.MockViewModel

@Composable
fun DashboardScreen(
    viewModel: MockViewModel,
    modifier: Modifier = Modifier,
    onNavigateToProfile: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val deviceStatus by viewModel.deviceStatus.collectAsState()
    val context = LocalContext.current
    var actionFeedback by remember { mutableStateOf<String?>(null) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var routineName by remember { mutableStateOf("") }
    var routineTrigger by remember { mutableStateOf("") }
    var routineCommandsText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
                Text(
                    text = user.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            IconButton(
                onClick = onNavigateToProfile,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
            ) {
                Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Device Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(PrimaryBlue.copy(alpha = 0.3f), AccentPurple.copy(alpha = 0.2f))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Device Status",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatusItem(label = "Battery", value = "${deviceStatus.battery}%")
                    StatusItem(label = "Network", value = "Strong")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Feedback
        AnimatedVisibility(visible = actionFeedback != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBlue.copy(alpha = 0.15f))
                    .padding(12.dp)
            ) {
                Text(text = actionFeedback ?: "", color = PrimaryBlue, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Quick Actions
        Text(
            text = "Quick Actions",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                title = "Camera",
                icon = Icons.Filled.CameraAlt,
                modifier = Modifier.weight(1f),
                onClick = {
                    try {
                        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        actionFeedback = "✅ Camera opened!"
                    } catch (e: Exception) {
                        actionFeedback = "❌ Could not open camera"
                    }
                }
            )
            QuickActionButton(
                title = "Wi-Fi",
                icon = Icons.Filled.Wifi,
                modifier = Modifier.weight(1f),
                onClick = {
                    try {
                        // Android 10+ does not allow apps to toggle Wi-Fi directly
                        // Open Wi-Fi settings panel instead
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Intent(Settings.Panel.ACTION_WIFI)
                        } else {
                            @Suppress("DEPRECATION")
                            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                            wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
                            null
                        }
                        intent?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(it)
                        }
                        actionFeedback = "✅ Wi-Fi panel opened!"
                    } catch (e: Exception) {
                        actionFeedback = "❌ Error: ${e.message}"
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                title = "YouTube",
                icon = Icons.Filled.PlayArrow,
                modifier = Modifier.weight(1f),
                onClick = {
                    try {
                        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                            actionFeedback = "✅ YouTube opened!"
                        } else {
                            actionFeedback = "❌ YouTube not installed"
                        }
                    } catch (e: Exception) {
                        actionFeedback = "❌ Error: ${e.message}"
                    }
                }
            )
            QuickActionButton(
                title = "Spotify",
                icon = Icons.Filled.MusicNote,
                modifier = Modifier.weight(1f),
                onClick = {
                    try {
                        val intent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                            actionFeedback = "✅ Spotify opened!"
                        } else {
                            actionFeedback = "❌ Spotify not installed"
                        }
                    } catch (e: Exception) {
                        actionFeedback = "❌ Error: ${e.message}"
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                title = "Flashlight",
                icon = Icons.Filled.FlashOn,
                modifier = Modifier.weight(1f),
                onClick = {
                    actionFeedback = "Use AI Chat: 'Turn on flashlight'"
                }
            )
            QuickActionButton(
                title = "Alarm",
                icon = Icons.Filled.Alarm,
                modifier = Modifier.weight(1f),
                onClick = {
                    try {
                        val intent = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        actionFeedback = "✅ Alarms opened!"
                    } catch (e: Exception) {
                        actionFeedback = "❌ Could not open alarms"
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                title = "Settings",
                icon = Icons.Filled.Person,
                modifier = Modifier.weight(0.5f), // spans full weight or alignment
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        actionFeedback = "✅ Settings opened!"
                    } catch (e: Exception) {
                        actionFeedback = "❌ Could not open settings"
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Routines Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI Routines (Macros)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            IconButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create Routine",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Routines list
        val routines by viewModel.routines.collectAsState()
        if (routines.isEmpty()) {
            Text(
                text = "No routines configured yet.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                routines.forEach { routine ->
                    RoutineCard(
                        routine = routine,
                        onRun = {
                            viewModel.runRoutineDirectly(routine) { feedback ->
                                actionFeedback = feedback
                            }
                        },
                        onDelete = {
                            viewModel.deleteRoutine(routine.trigger)
                            actionFeedback = "🗑️ Routine deleted!"
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "Create Custom Routine",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = routineName,
                        onValueChange = { routineName = it },
                        label = { Text("Routine Name (e.g. Work Mode)", color = Color.Gray) },
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
                    OutlinedTextField(
                        value = routineTrigger,
                        onValueChange = { routineTrigger = it },
                        label = { Text("Trigger word (e.g. work time)", color = Color.Gray) },
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
                    OutlinedTextField(
                        value = routineCommandsText,
                        onValueChange = { routineCommandsText = it },
                        label = { Text("Commands (comma separated)", color = Color.Gray) },
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
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (routineName.isNotBlank() && routineTrigger.isNotBlank() && routineCommandsText.isNotBlank()) {
                            val cmds = routineCommandsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            viewModel.addCustomRoutine(routineName, routineTrigger, cmds)
                            actionFeedback = "✅ Custom routine \"$routineName\" added!"
                            showCreateDialog = false
                            routineName = ""
                            routineTrigger = ""
                            routineCommandsText = ""
                        }
                    }
                ) {
                    Text("Save", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = SurfaceDark,
            tonalElevation = 6.dp
        )
    }
}

@Composable
fun RoutineCard(
    routine: com.aimobile.utils.Routine,
    onRun: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                ambientColor = Color.Black,
                spotColor = PrimaryBlue
            )
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Trigger: \"${routine.trigger}\"",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRun,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Run Routine",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Routine",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Steps:",
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            routine.commands.forEachIndexed { index, cmd ->
                Row(
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}. ",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = cmd,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatusItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = PrimaryBlue, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                ambientColor = Color.Black,
                spotColor = PrimaryBlue
            )
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            // Simulated 3D Bevel/reflection border
            .border(1.2.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryBlue,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
