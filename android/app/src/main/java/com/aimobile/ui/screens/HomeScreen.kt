package com.aimobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun HomeScreen() {
    // Pulsing animation for the connected dot — keep existing logic
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.15f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse_scale"
    )

    val context = androidx.compose.ui.platform.LocalContext.current
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val onRefreshAction: () -> Unit = {
        refreshing = true
        scope.launch {
            kotlinx.coroutines.delay(800) // Brief delay to show animation
            // Restart app logic
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(context.packageName)
            val componentName = intent?.component
            val mainIntent = android.content.Intent.makeRestartActivityTask(componentName)
            context.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Agent.AI",
                        fontWeight    = FontWeight.ExtraBold,
                        fontSize      = 22.sp,
                        color         = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor      = Background,
                    titleContentColor   = TextPrimary
                ),
                actions = {
                    IconButton(
                        onClick   = { /* TODO */ },
                        modifier  = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBg)
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = TextSub)
                    }
                }
            )
        }
    ) { paddingValues ->
        com.google.accompanist.swiperefresh.SwipeRefresh(
            state = com.google.accompanist.swiperefresh.rememberSwipeRefreshState(isRefreshing = refreshing),
            onRefresh = onRefreshAction,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(listOf(Background, Color(0xFF060608)))),
            indicator = { state, trigger ->
                com.google.accompanist.swiperefresh.SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    backgroundColor = CardBg,
                    contentColor = Accent
                )
            }
        ) {
            // Standard vertical scroll column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                // AI Orb Interface
                OrbDashboard()

                Spacer(Modifier.height(24.dp))

                // Listening state indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBg)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        color       = Accent,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text       = "Awaiting commands via Socket…",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = TextPrimary
                    )
                }

                Spacer(Modifier.height(50.dp))

                // Secure badge
                Row(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(CardBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = TextSub, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text       = "Background service active & encrypted",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color      = TextSub
                    )
                }
            }
        }
    }
}


@Composable
fun OrbDashboard(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "orb_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "orb_pulse"
    )

    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top cursive text
        Text(
            text = "AI Agent",
            fontSize = 38.sp,
            color = Color.White,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left progress
            CircularStat(
                progress = 0f, 
                valueText = "0", 
                totalText = "/ 8", 
                accentColor = Color(0xFFFF4081)
            )

            // Central Orb
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating arcs
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rotation }
                ) {
                    val strokeW = 4.dp.toPx()
                    val pad = strokeW / 2f
                    
                    // Arc 1
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0x33FFD700))),
                        startAngle = 0f,
                        sweepAngle = 140f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                        topLeft = androidx.compose.ui.geometry.Offset(pad, pad),
                        size = androidx.compose.ui.geometry.Size(size.width - strokeW, size.height - strokeW)
                    )
                    // Arc 2
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0x33FFD700))),
                        startAngle = 180f,
                        sweepAngle = 140f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                        topLeft = androidx.compose.ui.geometry.Offset(pad, pad),
                        size = androidx.compose.ui.geometry.Size(size.width - strokeW, size.height - strokeW)
                    )
                }

                // Inner pulsing orb
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .scale(pulseScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, Color.White.copy(alpha = 0.8f))
                            ),
                            shape = CircleShape
                        )
                ) {
                    // Small blue dot inside
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(4.dp)
                                .background(Color(0xFF42A5F5))
                        )
                    }
                }
            }

            // Right progress
            CircularStat(
                progress = 5f / 11f, 
                valueText = "5", 
                totalText = "/ 11", 
                accentColor = Color(0xFFFFD700)
            )
        }
    }
}

@Composable
fun CircularStat(progress: Float, valueText: String, totalText: String, accentColor: Color) {
    Box(
        modifier = Modifier.size(70.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = 1f,
            modifier = Modifier.fillMaxSize(),
            color = Color.DarkGray.copy(alpha = 0.4f),
            strokeWidth = 3.dp,
        )
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
            color = accentColor,
            strokeWidth = 3.dp,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(valueText, color = accentColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(totalText, color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
        }
    }
}
