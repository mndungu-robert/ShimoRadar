package com.example.shimoradar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shimoradar.Routes
import com.example.shimoradar.ui.theme.CardWhite
import com.example.shimoradar.ui.theme.CoralDark
import com.example.shimoradar.ui.theme.CoralTint
import com.example.shimoradar.ui.theme.Muted

@Composable
fun AdminNavBar(
    currentRoute: String?,
    onTabClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            shape = RoundedCornerShape(26.dp),
            color = CardWhite,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(Icons.Filled.Home, "Dashboard", currentRoute == Routes.ADMIN_DASH) { onTabClick(Routes.ADMIN_DASH) }
                NavItem(Icons.AutoMirrored.Filled.List, "Reports", currentRoute == Routes.ADMIN_REPORTS) { onTabClick(Routes.ADMIN_REPORTS) }
                NavItem(Icons.Filled.LocationOn, "Map", currentRoute == Routes.ADMIN_MAP) { onTabClick(Routes.ADMIN_MAP) }
                NavItem(Icons.Filled.Person, "Account", currentRoute == Routes.ADMIN_ACCOUNT) { onTabClick(Routes.ADMIN_ACCOUNT) }
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) CoralDark else Muted
    Column(
        modifier = Modifier
            .size(width = 62.dp, height = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) CoralTint else Color.Transparent)
            .clickable(onClick = onClick, role = Role.Tab),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Text(label, color = tint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
