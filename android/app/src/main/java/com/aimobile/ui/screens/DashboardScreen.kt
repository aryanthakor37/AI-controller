package com.aimobile.ui.screens

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.aimobile.R
import com.aimobile.ui.components.*
import com.aimobile.ui.theme.*
import com.aimobile.ui.viewmodel.MockViewModel
import com.aimobile.utils.HapticHelper
import com.aimobile.utils.HapticType

@Composable
fun DashboardScreen(
    viewModel: MockViewModel,
    modifier: Modifier = Modifier,
    onNavigateToProfile: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val deviceStatus by viewModel.deviceStatus.collectAsState()
    val context = LocalContext.current

    val vmFeedback by viewModel.lastActionFeedback.collectAsState()
    var actionFeedback by remember { mutableStateOf<String?>(null) }
    var islandState by remember { mutableStateOf(DynamicIslandState.IDLE) }
    var triggerCelebration by remember { mutableStateOf(false) }

    LaunchedEffect(vmFeedback) {
        val fb = vmFeedback
        if (!fb.isNullOrBlank()) {
            actionFeedback = fb
            val isSuccess = fb.startsWith("✅") || fb.startsWith("🌤️") || fb.startsWith("📰") || fb.startsWith("📅") || fb.startsWith("🌙") || fb.startsWith("🎵")
            islandState = if (isSuccess) DynamicIslandState.SUCCESS else DynamicIslandState.PROCESSING
            if (isSuccess) {
                triggerCelebration = true
                HapticHelper.performHaptic(context, HapticType.SUCCESS)
            }
            kotlinx.coroutines.delay(4000)
            actionFeedback = null
            islandState = DynamicIslandState.IDLE
            triggerCelebration = false
            viewModel.clearActionFeedback()
        }
    }

    LaunchedEffect(actionFeedback) {
        if (actionFeedback != null && vmFeedback == null) {
            val fb = actionFeedback
            val isSuccess = fb?.startsWith("✅") == true || fb?.startsWith("🌤️") == true || fb?.startsWith("📰") == true
            islandState = if (isSuccess) DynamicIslandState.SUCCESS else DynamicIslandState.PROCESSING
            if (isSuccess) {
                triggerCelebration = true
                HapticHelper.performHaptic(context, HapticType.SUCCESS)
            }
            kotlinx.coroutines.delay(3500)
            actionFeedback = null
            islandState = DynamicIslandState.IDLE
            triggerCelebration = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // AI Tech Cybernetic Wallpaper Background
        Image(
            painter = painterResource(id = R.drawable.bg_ai_tech),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.35f,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // Dynamic Island Pill Top Anchor
            DynamicIslandPopup(
                state = islandState,
                message = actionFeedback ?: "AI Assistant Ready",
                onClick = { islandState = DynamicIslandState.IDLE }
            )

            Spacer(Modifier.height(12.dp))

            // ── Time-based Header ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmartGreetingHeader(userName = user.name)

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(8.dp, CircleShape, spotColor = Primary.copy(alpha = 0.3f))
                        .background(Brush.linearGradient(listOf(Primary, Secondary)), CircleShape)
                        .clickable {
                            HapticHelper.performHaptic(context, HapticType.LIGHT)
                            onNavigateToProfile()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.firstOrNull()?.toString() ?: "U",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── AI Smart Suggestions & Predictions ───────────────────────────────
            SmartSuggestionsRow(
                onSuggestionClick = { command ->
                    HapticHelper.performHaptic(context, HapticType.MEDIUM)
                    actionFeedback = "⏳ Processing: $command…"
                    islandState = DynamicIslandState.PROCESSING
                    viewModel.sendMessage(command)
                }
            )

            Spacer(Modifier.height(20.dp))

            // ── Device Status Card ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Primary.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Primary.copy(alpha = 0.25f), Secondary.copy(alpha = 0.18f))))
                    .border(1.dp, Primary.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(Success, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("Connected Device Status", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Text("Vivo V2250", color = TextSub, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(Modifier.height(18.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        StatusItem(label = "Battery", value = "${deviceStatus.battery}%", icon = Icons.Rounded.Power)
                        Box(Modifier.width(1.dp).height(36.dp).background(BorderColor))
                        StatusItem(label = "Network", value = "5G Strong", icon = Icons.Rounded.Wifi)
                        Box(Modifier.width(1.dp).height(36.dp).background(BorderColor))
                        StatusItem(label = "Agent State", value = "Active", icon = Icons.Rounded.CheckCircle)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── System Metrics Grid ──────────────────────────────────────────────
            Text("System Metrics & Stats", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "RAM Usage",
                    value = "4.2 / 8 GB",
                    progress = 0.52f,
                    color = Primary,
                    icon = Icons.Rounded.Build,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Storage",
                    value = "64 / 128 GB",
                    progress = 0.50f,
                    color = Secondary,
                    icon = Icons.Rounded.Folder,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "AI Usage",
                    value = "142 Requests",
                    progress = 0.70f,
                    color = Accent,
                    icon = Icons.Rounded.AutoAwesome,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Commands Today",
                    value = "28 Executed",
                    progress = 0.85f,
                    color = Success,
                    icon = Icons.Rounded.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Quick Actions Grid ────────────────────────────────────────────────
            Text("Quick Controls", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionButton(
                    title = "Camera", icon = Icons.Filled.CameraAlt, modifier = Modifier.weight(1f),
                    onClick = {
                        HapticHelper.performHaptic(context, HapticType.MEDIUM)
                        try {
                            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                            context.startActivity(intent)
                            actionFeedback = "✅ Camera opened!"
                        } catch (e: Exception) { actionFeedback = "❌ Could not open camera" }
                    }
                )
                QuickActionButton(
                    title = "Wi-Fi", icon = Icons.Filled.Wifi, modifier = Modifier.weight(1f),
                    onClick = {
                        HapticHelper.performHaptic(context, HapticType.MEDIUM)
                        try {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                Intent(Settings.Panel.ACTION_WIFI)
                            } else {
                                @Suppress("DEPRECATION")
                                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                                wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
                                null
                            }
                            intent?.let { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); context.startActivity(it) }
                            actionFeedback = "✅ Wi-Fi panel opened!"
                        } catch (e: Exception) { actionFeedback = "❌ Error: ${e.message}" }
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionButton(
                    title = "YouTube", icon = Icons.Filled.PlayArrow, modifier = Modifier.weight(1f),
                    onClick = {
                        HapticHelper.performHaptic(context, HapticType.MEDIUM)
                        try {
                            val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                            if (intent != null) { intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); context.startActivity(intent); actionFeedback = "✅ YouTube opened!" }
                            else { actionFeedback = "❌ YouTube not installed" }
                        } catch (e: Exception) { actionFeedback = "❌ Error: ${e.message}" }
                    }
                )
                QuickActionButton(
                    title = "Spotify", icon = Icons.Filled.MusicNote, modifier = Modifier.weight(1f),
                    onClick = {
                        HapticHelper.performHaptic(context, HapticType.MEDIUM)
                        try {
                            val intent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                            if (intent != null) { intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); context.startActivity(intent); actionFeedback = "✅ Spotify opened!" }
                            else { actionFeedback = "❌ Spotify not installed" }
                        } catch (e: Exception) { actionFeedback = "❌ Error: ${e.message}" }
                    }
                )
            }

            Spacer(Modifier.height(28.dp))
        }

        // Celebration Confetti Canvas
        CelebrationAnimation(
            trigger = triggerCelebration,
            onFinished = { triggerCelebration = false }
        )
    }
}

@Composable
fun StatusItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = TextSub, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = label, color = TextSub, fontSize = 11.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(text = value, color = Primary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    progress: Float,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(17.dp))
                }
                Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            Text(text = title, color = TextSub, fontSize = 12.sp, fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "qa_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black, spotColor = Primary.copy(alpha = 0.2f))
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .clickable {
                pressed = true
                onClick()
                pressed = false
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(Primary.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = Primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(text = title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
