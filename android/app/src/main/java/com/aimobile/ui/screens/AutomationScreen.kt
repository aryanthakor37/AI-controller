package com.aimobile.ui.screens

import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aimobile.ui.components.GlassCard
import com.aimobile.ui.theme.*
import com.aimobile.ui.viewmodel.AutomationViewModel
import com.aimobile.models.RoutineItem
import com.aimobile.utils.HapticHelper
import com.aimobile.utils.HapticType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    viewModel: AutomationViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.routines.collectAsState()
    val executingId by viewModel.executingRoutineId.collectAsState()
    val feedback by viewModel.executionFeedback.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editingRoutine by remember { mutableStateOf<RoutineItem?>(null) }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // ── Top Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(Modifier.width(4.dp))
                Column {
                    Text("Automations", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Multi-action routines & schedules", color = TextSub, fontSize = 12.sp)
                }
            }

            Button(
                onClick = {
                    editingRoutine = null
                    showDialog = true
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("New Routine", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Execution Progress Banner ─────────────────────────────────────────
        AnimatedVisibility(visible = feedback != null) {
            feedback?.let { text ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Primary.copy(alpha = 0.18f))
                        .border(1.dp, Primary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Accent,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(text = text, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        // ── Location Geofencing Section ─────────────────────────────────────────
        LocationGeofenceCard()
        Spacer(Modifier.height(16.dp))

        // ── Routines List ─────────────────────────────────────────────────────
        if (routines.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No automation routines set. Tap 'New Routine' to create one!", color = TextSub, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(routines, key = { it.id }) { routine ->
                    val isExecuting = executingId == routine.id

                    RoutineCard(
                        routine = routine,
                        isExecuting = isExecuting,
                        onToggle = { viewModel.toggleRoutine(routine.id) },
                        onRun = {
                            HapticHelper.performHaptic(context, HapticType.HEAVY)
                            viewModel.executeRoutine(routine)
                        },
                        onEdit = {
                            editingRoutine = routine
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteRoutine(routine.id) }
                    )
                }
            }
        }
    }

    // ── Create / Edit Routine Dialog ─────────────────────────────────────────
    if (showDialog) {
        RoutineEditorDialog(
            routine = editingRoutine,
            onDismiss = { showDialog = false },
            onSave = { newRoutine ->
                viewModel.addOrUpdateRoutine(newRoutine)
                showDialog = false
            }
        )
    }
}

@Composable
fun RoutineCard(
    routine: RoutineItem,
    isExecuting: Boolean,
    onToggle: () -> Unit,
    onRun: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val categoryIcon = when (routine.category) {
        "Morning" -> Icons.Rounded.WbSunny
        "Night" -> Icons.Rounded.NightsStay
        "Focus" -> Icons.Rounded.Psychology
        else -> Icons.Rounded.AutoAwesome
    }

    val categoryColor = when (routine.category) {
        "Morning" -> Accent
        "Night" -> Secondary
        "Focus" -> Primary
        else -> Success
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = categoryColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(categoryColor.copy(alpha = 0.16f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(categoryIcon, contentDescription = null, tint = categoryColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(text = routine.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "⏰ ${routine.scheduleTime}", color = TextSub, fontSize = 12.sp)
                    }
                }

                Switch(
                    checked = routine.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary)
                )
            }

            if (routine.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(text = routine.description, color = TextSub, fontSize = 12.sp)
            }

            Spacer(Modifier.height(10.dp))

            // Actions Count & Expand Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ ${routine.actions.size} Actions configured",
                    color = Accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (expanded) "Hide Actions" else "View Actions",
                        color = TextSub,
                        fontSize = 11.sp
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = TextSub,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expandable Actions List
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    routine.actions.forEachIndexed { idx, act ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Background.copy(alpha = 0.6f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = "${idx + 1}. $act", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Footer Controls (Run Now, Edit, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Last Run: ${routine.lastRun}", color = TextSub.copy(alpha = 0.6f), fontSize = 10.sp)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = TextSub, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Danger.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                    Button(
                        onClick = onRun,
                        enabled = !isExecuting,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Run Now", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorDialog(
    routine: RoutineItem?,
    onDismiss: () -> Unit,
    onSave: (RoutineItem) -> Unit
) {
    var name by remember { mutableStateOf(routine?.name ?: "") }
    var description by remember { mutableStateOf(routine?.description ?: "") }
    var scheduleTime by remember { mutableStateOf(routine?.scheduleTime ?: "08:00 AM Daily") }
    var category by remember { mutableStateOf(routine?.category ?: "Morning") }
    var actionsRaw by remember { mutableStateOf(routine?.actions?.joinToString("\n") ?: "What is the weather today?\nTurn on Flashlight") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (routine == null) "Create Routine" else "Edit Routine", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = scheduleTime,
                    onValueChange = { scheduleTime = it },
                    label = { Text("Schedule Time (e.g. 07:00 AM Daily)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = actionsRaw,
                    onValueChange = { actionsRaw = it },
                    label = { Text("Action Commands (One per line)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val actionsList = actionsRaw.lines().map { it.trim() }.filter { it.isNotBlank() }
                    val item = RoutineItem(
                        id = routine?.id ?: System.currentTimeMillis().toString(),
                        name = name.ifBlank { "Custom Routine" },
                        description = description,
                        scheduleTime = scheduleTime,
                        actions = if (actionsList.isNotEmpty()) actionsList else listOf("What is the weather today?"),
                        isEnabled = routine?.isEnabled ?: true,
                        category = category,
                        lastRun = routine?.lastRun ?: "Never"
                    )
                    onSave(item)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save Routine")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSub)
            }
        },
        containerColor = CardBg
    )
}

@Composable
fun LocationGeofenceCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHandler = remember { com.aimobile.handlers.LocationAutomationHandler(context) }
    val storage = remember { com.aimobile.data.LocationRuleStorage(context) }

    var rules by remember { mutableStateOf(storage.loadRules()) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var isAddRuleDialogOpen by remember { mutableStateOf(false) }

    // Dialog state for adding custom rule
    var newName by remember { mutableStateOf("") }
    var newLat by remember { mutableStateOf("") }
    var newLng by remember { mutableStateOf("") }
    var newRadius by remember { mutableStateOf("150") }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = Primary
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Primary.copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Live GPS Location Automation", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Real-Time GPS Auto-Silent Engine (100% Free)", color = TextSub, fontSize = 12.sp)
                    }
                }

                IconButton(onClick = { isAddRuleDialogOpen = true }) {
                    Icon(Icons.Rounded.AddLocation, contentDescription = "Add Location", tint = Accent)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Display List of Real Saved Location Rules
            if (rules.isEmpty()) {
                Text("No custom location rules saved. Tap '+' above to add your location!", color = TextSub, fontSize = 12.sp)
            } else {
                rules.forEach { rule ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Background.copy(alpha = 0.5f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(rule.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(6.dp))
                                Text("(${rule.radiusMeters.toInt()}m Radius)", color = TextSub, fontSize = 11.sp)
                            }
                            Text(
                                text = "GPS: ${rule.latitude.toString().take(7)}, ${rule.longitude.toString().take(7)} | Action: ${rule.enterAction}",
                                color = Accent,
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = rule.isEnabled,
                                onCheckedChange = { isChecked ->
                                    val updated = rules.map { if (it.id == rule.id) it.copy(isEnabled = isChecked) else it }
                                    rules = updated
                                    storage.saveRules(updated)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary)
                            )
                            IconButton(
                                onClick = {
                                    val updated = rules.filter { it.id != rule.id }
                                    rules = updated
                                    storage.saveRules(updated)
                                }
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = TextSub.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Real Live GPS Hardware Check Controls
            Button(
                onClick = {
                    scope.launch {
                        val res = locationHandler.checkRealLocationAndApplyRules()
                        statusText = res.message
                        android.widget.Toast.makeText(context, statusText, android.widget.Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Rounded.GpsFixed, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("📍 Check Live GPS Location & Apply Rules", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            statusText?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(msg, color = Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // Modal Dialog: Add Real Custom Location Rule
    if (isAddRuleDialogOpen) {
        AlertDialog(
            onDismissRequest = { isAddRuleDialogOpen = false },
            title = {
                Text("Add Custom Location Rule", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Location Name (e.g. My Office, College)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val gps = locationHandler.getCurrentGpsLocation()
                            if (gps != null) {
                                newLat = gps.latitude.toString()
                                newLng = gps.longitude.toString()
                                android.widget.Toast.makeText(context, "Captured Current GPS: ${gps.latitude.toString().take(7)}, ${gps.longitude.toString().take(7)}", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, "Turn ON GPS/Location on phone first", android.widget.Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                    ) {
                        Icon(Icons.Rounded.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("📍 Capture Current GPS Location", fontSize = 11.sp, color = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newLat,
                            onValueChange = { newLat = it },
                            label = { Text("Latitude") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newLng,
                            onValueChange = { newLng = it },
                            label = { Text("Longitude") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = newRadius,
                        onValueChange = { newRadius = it },
                        label = { Text("Trigger Radius in Meters (e.g. 150)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isBlank() || newLat.isBlank() || newLng.isBlank()) {
                            android.widget.Toast.makeText(context, "Please enter name and GPS coordinates", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val latVal = newLat.toDoubleOrNull() ?: 23.0225
                        val lngVal = newLng.toDoubleOrNull() ?: 72.5714
                        val radVal = newRadius.toFloatOrNull() ?: 150f

                        val newRule = com.aimobile.models.LocationRule(
                            id = System.currentTimeMillis().toString(),
                            name = newName,
                            latitude = latVal,
                            longitude = lngVal,
                            radiusMeters = radVal,
                            enterAction = "SILENT",
                            exitAction = "NORMAL_SOUND",
                            isEnabled = true
                        )
                        val updated = rules + newRule
                        rules = updated
                        storage.saveRules(updated)
                        isAddRuleDialogOpen = false
                        newName = ""
                        newLat = ""
                        newLng = ""
                        android.widget.Toast.makeText(context, "Custom Location '$newName' Saved!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Save Location Rule", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddRuleDialogOpen = false }) {
                    Text("Cancel", color = TextSub)
                }
            },
            containerColor = CardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

