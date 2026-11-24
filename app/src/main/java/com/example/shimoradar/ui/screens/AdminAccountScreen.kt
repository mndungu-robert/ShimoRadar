package com.example.shimoradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shimoradar.ui.theme.CardWhite
import com.example.shimoradar.ui.theme.Coral
import com.example.shimoradar.ui.theme.Muted
import com.example.shimoradar.ui.theme.Navy

@Composable
fun AdminAccountScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(Navy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("K", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Coral)
        }
        Spacer(Modifier.height(12.dp))
        Text("KURA Authority", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Text("Administrator view", fontSize = 14.sp, color = Muted)

        Spacer(Modifier.height(24.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = CardWhite,
            shadowElevation = 2.dp
        ) {
            Text(
                text = "Open any report from the Dashboard, Reports or Map and use \"Update status\" to move it from Reported to Under repair to Repaired. Citizens see the change straight away.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Navy,
                modifier = Modifier.padding(18.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onExit,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Switch to citizen app", fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}