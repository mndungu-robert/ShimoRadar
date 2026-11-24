package com.example.shimoradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shimoradar.data.ADMIN_PIN
import com.example.shimoradar.data.MockData
import com.example.shimoradar.data.ReportStatus
import com.example.shimoradar.ui.theme.CardWhite
import com.example.shimoradar.ui.theme.Coral
import com.example.shimoradar.ui.theme.CoralDark
import com.example.shimoradar.ui.theme.Muted
import com.example.shimoradar.ui.theme.Navy

@Composable
fun ProfileScreen(
    onAdminAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reports = MockData.reports
    var notifications by rememberSaveable { mutableStateOf(true) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var showPin by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(Coral, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("R", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        }
        Spacer(Modifier.height(12.dp))
        Text("Robert", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Text("Ruiru", fontSize = 14.sp, color = Muted)

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(reports.size.toString(), "Reports", Modifier.weight(1f))
            StatTile(
                reports.count { it.status == ReportStatus.UNDER_REPAIR }.toString(),
                "In repair",
                Modifier.weight(1f)
            )
            StatTile(
                reports.count { it.status == ReportStatus.REPAIRED }.toString(),
                "Fixed",
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = CardWhite,
            shadowElevation = 2.dp
        ) {
            Column {
                SettingRow(label = "Notifications", onClick = { notifications = !notifications }) {
                    Switch(
                        checked = notifications,
                        onCheckedChange = { notifications = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CoralDark
                        )
                    )
                }
                HorizontalDivider(color = Color(0xFFE2E8F0))
                SettingRow(label = "KURA admin access", onClick = { showPin = true }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
                }
                HorizontalDivider(color = Color(0xFFE2E8F0))
                SettingRow(label = "About ShimoRadar", onClick = { showAbout = true }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
                }
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("Close") }
            },
            title = { Text("ShimoRadar") },
            text = { Text("Report damaged roads in your area and follow their repair status. Version 1.0") }
        )
    }

    if (showPin) {
        var pin by remember { mutableStateOf("") }
        var wrong by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showPin = false },
            title = { Text("KURA admin access") },
            text = {
                Column {
                    Text("Enter the admin PIN to continue.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            pin = it.filter(Char::isDigit).take(4)
                            wrong = false
                        },
                        singleLine = true,
                        isError = wrong,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        supportingText = { if (wrong) Text("Wrong PIN") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pin == ADMIN_PIN) {
                        showPin = false
                        onAdminAccess()
                    } else {
                        wrong = true
                    }
                }) { Text("Enter") }
            },
            dismissButton = {
                TextButton(onClick = { showPin = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
            Text(label, fontSize = 12.sp, color = Muted)
        }
    }
}

@Composable
private fun SettingRow(
    label: String,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Navy)
        trailing()
    }
}