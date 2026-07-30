package com.aimobile.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.handlers.ReminderHandler
import com.aimobile.ui.theme.*
import kotlinx.coroutines.launch

data class AndroidReminderItem(
    val id: String,
    val title: String,
    val date: String,
    val time: String,
    val repeat: String,
    val contact: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reminderHandler = remember { ReminderHandler(context) }

    var searchQuery by remember { mutableStateOf("") }
    var isAddModalOpen by remember { mutableStateOf(false) }

    // Mock/Sample initial list of reminders
    var remindersList by remember {
        mutableStateOf(
            listOf(
                AndroidReminderItem("1", "Mom's Birthday", "2026-10-20", "10:00", "YEARLY", "Mom"),
                AndroidReminderItem("2", "Doctor Appointment", "2026-08-01", "16:00", "NONE", "Dr. Smith"),
                AndroidReminderItem("3", "Team Weekly Sync", "2026-08-03", "11:00", "WEEKLY", "Office")
            )
        )
    }

    // New Reminder Form State
    var newTitle by remember { mutableStateOf("") }
    var newDate by remember { mutableStateOf("") }
    var newTime by remember { mutableStateOf("09:00") }
    var newContact by remember { mutableStateOf("") }
    var newRepeat by remember { mutableStateOf("ONCE") }

    val filteredList = remindersList.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.contact.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Cake,
                        contentDescription = null,
                        tint = Color(0xFFF472B6),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Birthdays & Reminders",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "Manage upcoming birthdays & voice alerts on your phone",
                    color = TextSub,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = { isAddModalOpen = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFA855F7), Color(0xFFEC4899))
                        )
                    )
                    .size(42.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add", tint = Color.White)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Search Filter Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter reminders by title or contact...", fontSize = 13.sp, color = TextSub) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = TextSub) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedBorderColor = Primary,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(Modifier.height(20.dp))

        // Reminders Cards Grid
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Cake, contentDescription = null, tint = Color(0xFFF472B6), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No Reminders Found", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Tap the '+' button above to add a new birthday or reminder!", color = TextSub, fontSize = 12.sp)
                }
            }
        } else {
            filteredList.forEach { item ->
                val isBirthday = item.title.contains("birthday", ignoreCase = true) || item.repeat == "YEARLY"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isBirthday) Brush.linearGradient(
                                listOf(CardBg, Color(0x2BDB2777))
                            ) else Brush.linearGradient(listOf(CardBg, CardBg))
                        )
                        .border(
                            1.dp,
                            if (isBirthday) Color(0x66F472B6) else BorderColor,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (isBirthday) Color(0x33F472B6) else Primary.copy(alpha = 0.15f),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isBirthday) Icons.Rounded.Cake else Icons.Rounded.Notifications,
                                    contentDescription = null,
                                    tint = if (isBirthday) Color(0xFFF472B6) else Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(item.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = item.repeat,
                                    color = if (isBirthday) Color(0xFFF472B6) else TextSub,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                remindersList = remindersList.filter { it.id != item.id }
                                Toast.makeText(context, "Reminder removed", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = TextSub)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Divider(color = BorderColor, thickness = 0.5.dp)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(item.date, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(item.time, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        if (item.contact.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(item.contact, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    // Modal Popup: Add New Reminder
    if (isAddModalOpen) {
        AlertDialog(
            onDismissRequest = { isAddModalOpen = false },
            title = {
                Text("Add Birthday / Reminder", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title (e.g. Mom's Birthday)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDate,
                        onValueChange = { newDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTime,
                        onValueChange = { newTime = it },
                        label = { Text("Time (24h HH:MM)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newContact,
                        onValueChange = { newContact = it },
                        label = { Text("Contact Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isBlank()) {
                            Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val newItem = AndroidReminderItem(
                            id = System.currentTimeMillis().toString(),
                            title = newTitle,
                            date = newDate.ifBlank { "2026-10-20" },
                            time = newTime.ifBlank { "09:00" },
                            repeat = if (newTitle.contains("birthday", ignoreCase = true)) "YEARLY" else newRepeat,
                            contact = newContact
                        )
                        remindersList = remindersList + newItem
                        scope.launch {
                            reminderHandler.scheduleReminder(
                                title = newItem.title,
                                dateStr = newItem.date,
                                timeStr = newItem.time,
                                repeat = newItem.repeat,
                                contact = newItem.contact
                            )
                        }
                        Toast.makeText(context, "Reminder scheduled on Phone!", Toast.LENGTH_SHORT).show()
                        isAddModalOpen = false
                        newTitle = ""
                        newDate = ""
                        newContact = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Save Reminder", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddModalOpen = false }) {
                    Text("Cancel", color = TextSub)
                }
            },
            containerColor = CardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
