package com.aimobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.theme.*
import java.util.Calendar

data class CommandSuggestion(
    val title: String,
    val command: String,
    val icon: ImageVector,
    val category: String
)

@Composable
fun SmartGreetingHeader(
    userName: String,
    modifier: Modifier = Modifier
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning ☀️"
            in 12..16 -> "Good Afternoon 🌤️"
            in 17..21 -> "Good Evening 🌙"
            else -> "Late Night AI Agent 🌃"
        }
    }

    Column(modifier = modifier) {
        Text(text = greeting, color = TextSub, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = userName, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun SmartSuggestionsRow(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    val suggestions = remember(hour) {
        val timeBased = when (hour) {
            in 5..11 -> listOf(
                CommandSuggestion("Morning News", "Summarize morning news", Icons.Rounded.LightMode, "Routine"),
                CommandSuggestion("Check Weather", "What is the weather today?", Icons.Rounded.Cloud, "Weather")
            )
            in 12..16 -> listOf(
                CommandSuggestion("Lunch Break Alarms", "Set alarm for 2 PM", Icons.Rounded.Alarm, "Clock"),
                CommandSuggestion("Play Music", "Open Spotify and play focus music", Icons.Rounded.MusicNote, "Media")
            )
            else -> listOf(
                CommandSuggestion("Night Mode", "Turn on dark mode and set quiet alarm", Icons.Rounded.DarkMode, "System"),
                CommandSuggestion("Tomorrow Stats", "Summarize my calendar for tomorrow", Icons.Rounded.DateRange, "Schedule")
            )
        }

        timeBased + listOf(
            CommandSuggestion("Turn on WiFi", "Turn on WiFi connection", Icons.Rounded.Wifi, "Control"),
            CommandSuggestion("Toggle Setting", "Airplane mode on", Icons.Rounded.Settings, "Control")
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI Smart Suggestions", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Predicted", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(suggestions) { item ->
                SuggestionChipItem(
                    suggestion = item,
                    onClick = { onSuggestionClick(item.command) }
                )
            }
        }
    }
}

@Composable
fun SuggestionChipItem(
    suggestion: CommandSuggestion,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = suggestion.icon,
                contentDescription = suggestion.title,
                tint = Primary,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = suggestion.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = suggestion.category, color = TextSub, fontSize = 10.sp)
        }
    }
}
