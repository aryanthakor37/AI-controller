package com.aimobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.theme.AccentPurple
import com.aimobile.ui.theme.DarkBackground
import com.aimobile.ui.theme.PrimaryBlue
import com.aimobile.ui.theme.SurfaceDark
import com.aimobile.ui.viewmodel.MockChatMessage
import com.aimobile.ui.viewmodel.MockViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(viewModel: MockViewModel, modifier: Modifier = Modifier) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(messages.size - 1) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 12.dp, spotColor = PrimaryBlue)
                .background(SurfaceDark.copy(alpha = 0.85f))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape, spotColor = AccentPurple)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(PrimaryBlue, AccentPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("AI Assistant", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Powered by Gemini", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
        }

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message = message)
            }

            // Typing indicator
            if (isLoading) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                .background(SurfaceDark)
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Thinking", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = PrimaryBlue,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .shadow(elevation = 20.dp, shape = CircleShape, spotColor = PrimaryBlue)
                .background(SurfaceDark, CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask me to do something...", color = Color.White.copy(alpha = 0.4f), fontSize = 15.sp) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = {
                    val text = inputText.trim()
                    if (text.isNotBlank() && !isLoading) {
                        viewModel.sendMessage(text)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(52.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape, spotColor = AccentPurple)
                    .clip(CircleShape)
                    .background(
                        if (!isLoading) Brush.linearGradient(listOf(PrimaryBlue, AccentPurple))
                        else Brush.linearGradient(listOf(Color.Gray, Color.Gray))
                    )
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
fun ChatBubble(message: MockChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (message.isUser) {
            // User bubble — right side, 3D blue gradient
            Box(
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp), spotColor = PrimaryBlue)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Brush.linearGradient(listOf(PrimaryBlue, AccentPurple)))
                    .border(1.5.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)), RoundedCornerShape(topStart = 24.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Text(text = message.text, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
            }
        } else {
            // AI bubble — left side, 3D dark glass
            Box(
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(topStart = 6.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp), spotColor = Color.Black)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(SurfaceDark.copy(alpha = 0.9f))
                    .border(1.5.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)), RoundedCornerShape(topStart = 6.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Text(text = message.text, color = Color.White.copy(alpha = 0.95f), fontSize = 15.sp, lineHeight = 22.sp)
            }
        }
    }
}
