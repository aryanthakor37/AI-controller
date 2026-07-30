package com.aimobile.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.components.EmptyState
import com.aimobile.ui.components.EmptyStateType
import com.aimobile.ui.theme.*
import com.aimobile.ui.viewmodel.MockChatMessage
import com.aimobile.ui.viewmodel.MockViewModel
import com.aimobile.utils.HapticHelper
import com.aimobile.utils.HapticType
import kotlinx.coroutines.launch
import java.util.*

data class SmartCommandCatalogItem(
    val title: String,
    val command: String,
    val category: String
)

val smartCatalogItems = listOf(
    SmartCommandCatalogItem("Set Birthday Reminder", "Set a birthday reminder for my friend on October 25 at 10 AM", "Alarms"),
    SmartCommandCatalogItem("Summarize Screen", "Summarize what is on screen", "Device"),
    SmartCommandCatalogItem("Check Weather", "What is the weather today?", "Device"),
    SmartCommandCatalogItem("Morning News", "Summarize morning news", "System"),
    SmartCommandCatalogItem("Toggle Setting", "Airplane mode on", "System"),
    SmartCommandCatalogItem("Open Gallery", "Open gallery app", "Apps"),
    SmartCommandCatalogItem("Open YouTube", "Open YouTube app", "Apps"),
    SmartCommandCatalogItem("Play Spotify", "Open Spotify and play focus music", "Media"),
    SmartCommandCatalogItem("Turn on WiFi", "Turn on WiFi", "System"),
    SmartCommandCatalogItem("Turn off WiFi", "Turn off WiFi", "System"),
    SmartCommandCatalogItem("Turn on Bluetooth", "Turn on Bluetooth", "System"),
    SmartCommandCatalogItem("Turn off Bluetooth", "Turn off Bluetooth", "System"),
    SmartCommandCatalogItem("Set Alarm 7 AM", "Set alarm for 7 AM", "Alarms"),
    SmartCommandCatalogItem("Set Alarm 2 PM", "Set alarm for 2 PM", "Alarms"),
    SmartCommandCatalogItem("Flashlight On", "Turn on flashlight", "Device"),
    SmartCommandCatalogItem("Flashlight Off", "Turn off flashlight", "Device"),
    SmartCommandCatalogItem("Battery Status", "What is the battery level?", "Device"),
    SmartCommandCatalogItem("Tomorrow Stats", "Summarize my calendar for tomorrow", "System")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(viewModel: MockViewModel, modifier: Modifier = Modifier) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    val favorites by viewModel.favoriteCommands.collectAsState()
    val user by viewModel.user.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showSearchPanel by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var editingMessage by remember { mutableStateOf<MockChatMessage?>(null) }

    // Dynamic Time-Based Greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning ☀️"
            in 12..16 -> "Good Afternoon 🌤️"
            in 17..21 -> "Good Evening 🌙"
            else -> "Late Night AI Agent 🌃"
        }
    }

    // Auto-complete filtered list while typing
    val autoSuggestions = remember(inputText) {
        val query = inputText.trim().lowercase()
        if (query.length >= 2) {
            smartCatalogItems.filter {
                it.command.lowercase().contains(query) || it.title.lowercase().contains(query)
            }
        } else {
            emptyList()
        }
    }

    // Category Filtered Suggestions
    val categorySuggestions = remember(selectedCategory, searchQuery) {
        smartCatalogItems.filter { item ->
            val matchCategory = if (selectedCategory == "All") true else item.category.equals(selectedCategory, ignoreCase = true)
            val matchSearch = if (searchQuery.isBlank()) true
            else item.title.contains(searchQuery, ignoreCase = true) || item.command.contains(searchQuery, ignoreCase = true)
            matchCategory && matchSearch
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(messages.size - 1) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // ── Header with AI Greeting & Quick Tools ────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(8.dp, CircleShape, spotColor = Secondary.copy(alpha = 0.4f))
                            .background(Brush.linearGradient(listOf(Primary, Secondary)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Box(
                        Modifier
                            .size(10.dp)
                            .align(Alignment.BottomEnd)
                            .background(Success, CircleShape)
                            .border(2.dp, CardBg, CircleShape)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = greeting, color = TextSub, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(text = "AI Assistant • ${user.name}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                IconButton(onClick = { showSearchPanel = !showSearchPanel }) {
                    Icon(
                        imageVector = if (showSearchPanel) Icons.Rounded.Close else Icons.Rounded.Search,
                        contentDescription = "Search Catalog",
                        tint = if (showSearchPanel) Primary else TextSub
                    )
                }

                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Clear Chat", tint = TextSub)
                }
            }

            // Expandable Search & Analytics Toolbar
            AnimatedVisibility(visible = showSearchPanel) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search commands or suggestions…", color = TextSub, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = TextSub) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = TextSub)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Background,
                            unfocusedContainerColor = Background,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    // Analytics & Usage Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalyticsMiniPill("⚡ 142 Requests", Primary, Modifier.weight(1f))
                        AnalyticsMiniPill("✅ 28 Today", Success, Modifier.weight(1f))
                        AnalyticsMiniPill("⭐ ${favorites.size} Favorites", Accent, Modifier.weight(1f))
                    }
                }
            }
        }
        Divider(color = BorderColor, thickness = 1.dp)

        // ── Command Categories & Quick Suggestions Row ───────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg.copy(alpha = 0.6f))
                .padding(vertical = 8.dp)
        ) {
            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                items(listOf("All", "Device", "Apps", "Media", "Alarms", "System")) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Primary else CardBg)
                            .border(1.dp, if (isSelected) Primary else BorderColor, RoundedCornerShape(16.dp))
                            .clickable {
                                HapticHelper.performHaptic(context, HapticType.LIGHT)
                                selectedCategory = cat
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else TextSub,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Suggestion Chips Catalog
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                items(categorySuggestions) { item ->
                    val isFav = favorites.contains(item.command)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Background.copy(alpha = 0.8f))
                            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                            .clickable {
                                HapticHelper.performHaptic(context, HapticType.MEDIUM)
                                viewModel.sendMessage(item.command)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = if (isFav) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (isFav) Accent else TextSub,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    HapticHelper.performHaptic(context, HapticType.LIGHT)
                                    viewModel.toggleFavoriteCommand(item.command)
                                }
                        )
                    }
                }
            }
        }
        Divider(color = BorderColor, thickness = 0.5.dp)

        // ── Messages / Empty State ────────────────────────────────────────────
        if (messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                EmptyState(
                    type = EmptyStateType.CHAT,
                    onActionClick = {
                        inputText = "What is the weather today?"
                    },
                    actionText = "Check Weather Today"
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                itemsIndexed(messages, key = { index, msg -> "${msg.id}_$index" }) { _, message ->
                    EnhancedChatBubble(
                        message = message,
                        onCopy = { copyToClipboard(context, message.text) },
                        onShare = { shareText(context, message.text) },
                        onEdit = {
                            if (message.isUser) {
                                inputText = message.text
                                editingMessage = message
                            }
                        },
                        onRegenerate = {
                            if (!message.isUser) {
                                viewModel.sendMessage("Please regenerate the previous response")
                            }
                        }
                    )
                }

                if (isLoading) {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            TypingIndicator()
                        }
                    }
                }
            }
        }

        // ── Smart Auto-Complete Floating Row while typing ────────────────────
        AnimatedVisibility(visible = autoSuggestions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg.copy(alpha = 0.95f))
                    .border(1.dp, Primary.copy(alpha = 0.3f))
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = "Smart Suggestions:",
                    color = Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    items(autoSuggestions) { suggestion ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Primary.copy(alpha = 0.18f))
                                .border(1.dp, Primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable {
                                    inputText = suggestion.command
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = suggestion.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // ── Edit prompt pill banner ───────────────────────────────────────────
        if (editingMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Primary.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Editing prompt…",
                    color = Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = { editingMessage = null; inputText = "" },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Cancel edit", tint = Primary, modifier = Modifier.size(16.dp))
                }
            }
        }

        // ── Input Bar ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask me to do something…", color = TextSub, fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Background,
                    unfocusedContainerColor = Background,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Primary
                ),
                maxLines = 4
            )
            Spacer(Modifier.width(8.dp))

            var sendPressed by remember { mutableStateOf(false) }
            val sendScale by animateFloatAsState(
                targetValue = if (sendPressed) 0.88f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "send_scale"
            )
            val canSend = inputText.trim().isNotBlank() && !isLoading

            IconButton(
                onClick = {
                    val text = inputText.trim()
                    if (canSend) {
                        HapticHelper.performHaptic(context, HapticType.MEDIUM)
                        viewModel.sendMessage(text)
                        inputText = ""
                        editingMessage = null
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .scale(sendScale)
                    .shadow(8.dp, CircleShape, spotColor = Primary.copy(alpha = 0.35f))
                    .clip(CircleShape)
                    .background(
                        if (canSend) Brush.linearGradient(listOf(Primary, Secondary))
                        else Brush.linearGradient(listOf(BorderColor, BorderColor))
                    )
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AnalyticsMiniPill(label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Enhanced Chat Bubble with Code Blocks, Long Press Context Menu & Timestamps ─
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedChatBubble(
    message: MockChatMessage,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onRegenerate: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val timeFormatted = remember(message.time) {
        if (message.time.isNotBlank()) message.time else "Now"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                Modifier
                    .size(30.dp)
                    .background(Brush.linearGradient(listOf(Primary, Secondary)), CircleShape)
                    .align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                Text("AI", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
        }

        Box(modifier = Modifier.widthIn(max = 295.dp)) {
            Column(
                modifier = Modifier
                    .shadow(
                        elevation = if (message.isUser) 8.dp else 4.dp,
                        shape = if (message.isUser)
                            RoundedCornerShape(topStart = 20.dp, topEnd = 6.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                        else
                            RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                        spotColor = if (message.isUser) Primary.copy(alpha = 0.3f) else Color.Black
                    )
                    .clip(
                        if (message.isUser)
                            RoundedCornerShape(topStart = 20.dp, topEnd = 6.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                        else
                            RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    .background(
                        if (message.isUser) Brush.linearGradient(listOf(Primary, Secondary))
                        else Brush.linearGradient(listOf(CardBg, CardBg))
                    )
                    .border(
                        1.dp,
                        if (message.isUser) Color.White.copy(alpha = 0.20f) else BorderColor,
                        if (message.isUser)
                            RoundedCornerShape(topStart = 20.dp, topEnd = 6.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                        else
                            RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { showMenu = true }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Parse for code blocks (simple ``` detection)
                if (message.text.contains("```")) {
                    FormattedMarkdownContent(text = message.text)
                } else {
                    Text(
                        text = message.text,
                        color = if (message.isUser) Color.White else TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatted,
                        color = if (message.isUser) Color.White.copy(alpha = 0.7f) else TextSub.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }

            // Long Press Context Menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(CardBg)
            ) {
                DropdownMenuItem(
                    text = { Text("Copy", color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null, tint = Primary) },
                    onClick = { onCopy(); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Share", color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null, tint = Accent) },
                    onClick = { onShare(); showMenu = false }
                )
                if (message.isUser) {
                    DropdownMenuItem(
                        text = { Text("Edit Prompt", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = Secondary) },
                        onClick = { onEdit(); showMenu = false }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("Regenerate", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Success) },
                        onClick = { onRegenerate(); showMenu = false }
                    )
                }
            }
        }
    }
}

// Simple Markdown / Code block renderer
@Composable
fun FormattedMarkdownContent(text: String) {
    val context = LocalContext.current
    val parts = remember(text) { text.split("```") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Code block
                val codeLines = part.trim().lines()
                val lang = if (codeLines.isNotEmpty() && codeLines.first().length < 15 && !codeLines.first().contains(" ")) codeLines.first() else ""
                val codeText = if (lang.isNotEmpty()) codeLines.drop(1).joinToString("\n") else part.trim()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF09090D))
                        .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.ifEmpty { "code" }.uppercase(),
                                color = Accent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            IconButton(
                                onClick = { copyToClipboard(context, codeText) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = "Copy code", tint = TextSub, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = codeText,
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 17.sp
                        )
                    }
                }
            } else if (part.isNotBlank()) {
                Text(
                    text = part.trim(),
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Message"))
}

@Composable
fun TypingIndicator() {
    val infinite = rememberInfiniteTransition(label = "typing")
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(CardBg)
            .border(1.dp, BorderColor, RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(3) { idx ->
                val offsetY by infinite.animateFloat(
                    initialValue = 0f, targetValue = -6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400, delayMillis = idx * 120, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "dot_$idx"
                )
                Box(
                    Modifier
                        .size(7.dp)
                        .offset(y = offsetY.dp)
                        .background(Primary, CircleShape)
                )
            }
        }
    }
}
