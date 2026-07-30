package com.aimobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.theme.*
import com.aimobile.ui.viewmodel.MockViewModel

@Composable
fun ProfileScreen(viewModel: MockViewModel, modifier: Modifier = Modifier, onLogout: () -> Unit) {
    val user by viewModel.user.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Hero Banner with Pro Badge ────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.linearGradient(listOf(Primary.copy(alpha = 0.65f), Secondary.copy(alpha = 0.55f)))
                )
        ) {
            // Subscription Pro Chip on Top-Right
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.40f))
                    .border(1.dp, Primary.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Accent, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("PRO MEMBER", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Avatar centered at bottom of banner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 44.dp)
                    .size(88.dp)
                    .shadow(12.dp, CircleShape, spotColor = Primary.copy(alpha = 0.4f))
                    .border(3.dp, Background, CircleShape)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Primary, Secondary))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.firstOrNull()?.toString()?.uppercase() ?: "U",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(Modifier.height(52.dp))

        // ── Name & Email ──────────────────────────────────────────────────────
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(user.name, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(4.dp))
            Text(user.email, color = TextSub, fontSize = 14.sp)
        }

        Spacer(Modifier.height(24.dp))

        // ── Stats Row ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardBg)
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStatItem(label = "Devices", value = "${user.deviceCount}")
            Box(Modifier.width(1.dp).height(36.dp).background(BorderColor))
            ProfileStatItem(label = "Commands", value = "142")
            Box(Modifier.width(1.dp).height(36.dp).background(BorderColor))
            ProfileStatItem(label = "Tier", value = "Unlimited")
        }

        Spacer(Modifier.height(24.dp))

        // ── Achievements Section ──────────────────────────────────────────────
        Column(Modifier.padding(horizontal = 24.dp)) {
            Text("Achievements & Badges", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AchievementChip(title = "Early Adopter", icon = Icons.Rounded.Star, color = Accent, modifier = Modifier.weight(1f))
                AchievementChip(title = "Power Agent", icon = Icons.Rounded.Bolt, color = Success, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Account Info Cards ────────────────────────────────────────────────
        Column(Modifier.padding(horizontal = 24.dp)) {
            Text("Account Details", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            ProfileInfoRow(icon = Icons.Rounded.Person, label = "Full Name", value = user.name)
            Spacer(Modifier.height(10.dp))
            ProfileInfoRow(icon = Icons.Rounded.Email, label = "Email Address", value = user.email)
            Spacer(Modifier.height(10.dp))
            ProfileInfoRow(icon = Icons.Rounded.Devices, label = "Connected Device", value = "Vivo V2250 (${user.deviceCount} registered)")
        }

        Spacer(Modifier.height(24.dp))



        // ── Logout Button ─────────────────────────────────────────────────────
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Danger.copy(alpha = 0.12f))
                .border(1.dp, Danger.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
        ) {
            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Icon(Icons.Rounded.Logout, contentDescription = null, tint = Danger, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log Out", color = Danger, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextSub, fontSize = 12.sp)
    }
}

@Composable
fun AchievementChip(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(text = title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            Text(label, color = TextSub, fontSize = 11.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
