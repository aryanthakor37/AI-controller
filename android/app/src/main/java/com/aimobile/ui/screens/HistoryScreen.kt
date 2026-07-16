package com.aimobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.components.GlassCard
import com.aimobile.ui.theme.DarkBackground
import com.aimobile.ui.theme.PrimaryBlue
import com.aimobile.ui.viewmodel.CloudViewModel

@Composable
fun HistoryScreen(viewModel: CloudViewModel, modifier: Modifier = Modifier) {
    val commands by viewModel.historyList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllData()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text(text = "Command History", color = Color.White, fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (commands.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No command execution logs found.", color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(commands) { command ->
                    val displayTime = remember(command.createdAt) {
                        try {
                            val parts = command.createdAt.split("T")
                            if (parts.size == 2) {
                                val date = parts[0]
                                val time = parts[1].substringBefore(".")
                                "$date $time"
                            } else {
                                command.createdAt
                            }
                        } catch (e: Exception) {
                            command.createdAt
                        }
                    }

                    val statusColor = when (command.status ?: "Pending") {
                        "Success", "completed" -> Color.Green
                        "Pending" -> Color.Yellow
                        else -> Color.Red
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = command.command ?: "Unknown Command",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${(command.intent ?: "UNKNOWN_COMMAND").replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }} • ${command.deviceName ?: "Unknown Device"}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = displayTime,
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Text(
                                text = command.status ?: "Pending",
                                color = statusColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

