package com.example.shimoradar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shimoradar.ui.components.FloatingNavBar
import com.example.shimoradar.ui.screens.HomeScreen
import com.example.shimoradar.ui.screens.ProfileScreen
import com.example.shimoradar.ui.screens.ReportDetailScreen
import com.example.shimoradar.ui.screens.ReportFormScreen
import com.example.shimoradar.ui.screens.ReportsScreen
import com.example.shimoradar.ui.screens.ScanScreen
import com.example.shimoradar.ui.theme.Muted
import com.example.shimoradar.ui.theme.Navy

object Routes {
    const val HOME = "home"
    const val REPORTS = "reports"
    const val SCAN = "scan"
    const val MAP = "map"
    const val PROFILE = "profile"
    const val REPORT_DETAIL = "report/{id}"
    const val FORM = "form/{photo}"

    fun reportDetail(id: Int) = "report/$id"
    fun form(photoName: String) = "form/$photoName"
}

private val tabRoutes = listOf(Routes.HOME, Routes.REPORTS, Routes.MAP, Routes.PROFILE)

private fun NavHostController.goToTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun SmartRoadApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute in tabRoutes) {
                FloatingNavBar(
                    currentRoute = currentRoute,
                    onTabClick = { navController.goToTab(it) },
                    onScanClick = { navController.navigate(Routes.SCAN) }
                )
            }
        }
    ) { innerPadding ->
        // Only the top inset is applied, so content can scroll behind the floating bar.
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(
                top = if (currentRoute == Routes.SCAN) 0.dp else innerPadding.calculateTopPadding()
            )
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onScanClick = { navController.navigate(Routes.SCAN) },
                    onSeeAllClick = { navController.goToTab(Routes.REPORTS) },
                    onReportClick = { id -> navController.navigate(Routes.reportDetail(id)) }
                )
            }
            composable(Routes.REPORTS) {
                ReportsScreen(onReportClick = { id -> navController.navigate(Routes.reportDetail(id)) })
            }
            composable(
                route = Routes.REPORT_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                ReportDetailScreen(
                    reportId = entry.arguments?.getInt("id") ?: -1,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.MAP) { PlaceholderScreen("Map", "Built in Phase 4") }
            composable(Routes.PROFILE) { ProfileScreen() }
            composable(Routes.SCAN) {
                ScanScreen(
                    onBack = { navController.popBackStack() },
                    onCaptured = { photoName -> navController.navigate(Routes.form(photoName)) }
                )
            }
            composable(
                route = Routes.FORM,
                arguments = listOf(navArgument("photo") { type = NavType.StringType })
            ) { entry ->
                ReportFormScreen(
                    photoName = entry.arguments?.getString("photo") ?: "",
                    onBack = { navController.popBackStack() },
                    onSubmitted = { navController.goToTab(Routes.REPORTS) }
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Text(subtitle, color = Muted, modifier = Modifier.padding(top = 6.dp))
        if (onBack != null) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Color.White),
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text("Back")
            }
        }
    }
}