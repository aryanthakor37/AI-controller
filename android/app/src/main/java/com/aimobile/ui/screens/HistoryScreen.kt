package com.aimobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.api.HistoryItem
import com.aimobile.ui.components.EmptyState
import com.aimobile.ui.components.EmptyStateType
import com.aimobile.ui.components.HistoryItemSkeleton
import com.aimobile.ui.theme.*
import com.aimobile.ui.viewmodel.CloudViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: CloudViewModel, modifier: Modifier = Modifier) {
    val rawCommands by viewModel.historyList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var pinnedIds by remember { mutableStateOf(setOf<String>()) }
    var favoriteIds by remember { mutableStateOf(setOf<String>()) }
    var deletedIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        viewModel.loadAllData()
    }

    val filteredCommands = remember(rawCommands, searchQuery, selectedFilter, pinnedIds, favoriteIds, deletedIds) {
        rawCommands
            .filter { cmd -> !deletedIds.contains(cmd._id) }
            .filter { cmd ->
                if (searchQuery.isBlank()) true
                else (cmd.command ?: "").contains(searchQuery, ignoreCase = true) ||
                     (cmd.intent ?: "").contains(searchQuery, ignoreCase = true) ||
                     (cmd.deviceName ?: "").contains(searchQuery, ignoreCase = true)
            }
            .filter { cmd ->
                when (selectedFilter) {
                    "Success" -> (cmd.status ?: "").contains("success", ignoreCase = true) || (cmd.status ?: "").contains("completed", ignoreCase = true)
                    "Failed" -> (cmd.status ?: "").contains("failed", ignoreCase = true) || (cmd.status ?: "").contains("error", ignoreCase = true)
                    "Pinned" -> pinnedIds.contains(cmd._id)
                    "Favorites" -> favoriteIds.contains(cmd._id)
                    else -> true
                }
            }
            .sortedByDescending { cmd -> pinnedIds.contains(cmd._id) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.History, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text("Command History", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }
            IconButton(onClick = { viewModel.loadAllData() }) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Refresh History", tint = Primary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Instant Search Input ──────────────────────────────────────────────
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search commands, intent or device…", color = TextSub, fontSize = 13.sp) },
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
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedBorderColor = Primary,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Primary
            )
        )

        Spacer(Modifier.height(12.dp))

        // ── Category Filter Chips ─────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Success", "Failed", "Pinned", "Favorites").forEach { filterName ->
                val isSelected = selectedFilter == filterName
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filterName },
                    label = { Text(filterName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White,
                        containerColor = CardBg,
                        labelColor = TextSub
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = BorderColor,
                        selectedBorderColor = Primary
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── History List Content ──────────────────────────────────────────────
        when {
            isLoading -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(5) { HistoryItemSkeleton() }
                }
            }

            filteredCommands.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (searchQuery.isNotEmpty() || selectedFilter != "All") {
                        EmptyState(
                            type = EmptyStateType.SEARCH,
                            onActionClick = { searchQuery = ""; selectedFilter = "All" },
                            actionText = "Clear Search & Filters"
                        )
                    } else {
                        EmptyState(type = EmptyStateType.HISTORY)
                    }
                }
            }

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredCommands, key = { it._id }) { command ->
                        val isPinned = pinnedIds.contains(command._id)
                        val isFav = favoriteIds.contains(command._id)

                        HistoryCardItem(
                            command = command,
                            isPinned = isPinned,
                            isFavorite = isFav,
                            onTogglePin = {
                                pinnedIds = if (isPinned) pinnedIds - command._id else pinnedIds + command._id
                            },
                            onToggleFav = {
                                favoriteIds = if (isFav) favoriteIds - command._id else favoriteIds + command._id
                            },
                            onDelete = {
                                deletedIds = deletedIds + command._id
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCardItem(
    command: HistoryItem,
    isPinned: Boolean,
    isFavorite: Boolean,
    onTogglePin: () -> Unit,
    onToggleFav: () -> Unit,
    onDelete: () -> Unit
) {
    val displayTime = remember(command.createdAt) {
        try {
            val parts = command.createdAt.split("T")
            if (parts.size == 2) "${parts[0]} ${parts[1].substringBefore(".")}"
            else command.createdAt
        } catch (e: Exception) { command.createdAt }
    }

    val status = command.status ?: "Pending"
    val (statusColor, statusBg) = when {
        status.contains("success", ignoreCase = true) || status.contains("completed", ignoreCase = true) ->
            Pair(Success, Success.copy(alpha = 0.12f))
        status.contains("pending", ignoreCase = true) ->
            Pair(Accent, Accent.copy(alpha = 0.12f))
        else ->
            Pair(Danger, Danger.copy(alpha = 0.12f))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(
                1.dp,
                if (isPinned) Primary.copy(alpha = 0.5f) else BorderColor,
                RoundedCornerShape(16.dp)
            )
    ) {
        Box(
            Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(statusColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (isPinned) {
                        Icon(Icons.Rounded.PushPin, contentDescription = "Pinned", tint = Primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = command.command ?: "Unknown Command",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                }

                Box(
                    Modifier
                        .background(statusBg, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${(command.intent ?: "UNKNOWN").replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }} • ${command.deviceName ?: "Unknown"}",
                    color = TextSub,
                    fontSize = 12.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = "Pin",
                            tint = if (isPinned) Primary else TextSub,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onToggleFav, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Accent else TextSub,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = Danger.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(text = displayTime, color = TextSub.copy(alpha = 0.6f), fontSize = 10.sp)
        }
    }
}
