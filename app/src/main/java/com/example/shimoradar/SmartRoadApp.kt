package com.example.shimoradar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shimoradar.ui.components.AdminNavBar
import com.example.shimoradar.ui.components.FloatingNavBar
import com.example.shimoradar.ui.screens.AdminAccountScreen
import com.example.shimoradar.ui.screens.AdminDashboardScreen
import com.example.shimoradar.ui.screens.AdminReportsScreen
import com.example.shimoradar.ui.screens.HomeScreen
import com.example.shimoradar.ui.screens.MapScreen
import com.example.shimoradar.ui.screens.ProfileScreen
import com.example.shimoradar.ui.screens.ReportDetailScreen
import com.example.shimoradar.ui.screens.ReportFormScreen
import com.example.shimoradar.ui.screens.ReportsScreen
import com.example.shimoradar.ui.screens.ScanScreen

enum class Role { CITIZEN, ADMIN }

object Routes {
    const val HOME = "home"
    const val REPORTS = "reports"
    const val SCAN = "scan"
    const val MAP = "map"
    const val PROFILE = "profile"
    const val REPORT_DETAIL = "report/{id}"
    const val FORM = "form/{photo}"

    const val ADMIN_DASH = "admin/dashboard"
    const val ADMIN_REPORTS = "admin/reports"
    const val ADMIN_MAP = "admin/map"
    const val ADMIN_ACCOUNT = "admin/account"

    fun reportDetail(id: Int) = "report/$id"
    fun form(photoName: String) = "form/$photoName"
}

private val citizenTabs = listOf(Routes.HOME, Routes.REPORTS, Routes.MAP, Routes.PROFILE)
private val adminTabs = listOf(
    Routes.ADMIN_DASH, Routes.ADMIN_REPORTS, Routes.ADMIN_MAP, Routes.ADMIN_ACCOUNT
)

private fun NavHostController.goToTab(route: String, root: String) {
    navigate(route) {
        popUpTo(root) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun SmartRoadApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var role by rememberSaveable { mutableStateOf(Role.CITIZEN) }

    fun switchRole(newRole: Role) {
        role = newRole
        navController.navigate(if (newRole == Role.ADMIN) Routes.ADMIN_DASH else Routes.HOME) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (role == Role.CITIZEN && currentRoute in citizenTabs) {
                FloatingNavBar(
                    currentRoute = currentRoute,
                    onTabClick = { navController.goToTab(it, Routes.HOME) },
                    onScanClick = { navController.navigate(Routes.SCAN) }
                )
            } else if (role == Role.ADMIN && currentRoute in adminTabs) {
                AdminNavBar(
                    currentRoute = currentRoute,
                    onTabClick = { navController.goToTab(it, Routes.ADMIN_DASH) }
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
            // ---- Citizen ----
            composable(Routes.HOME) {
                HomeScreen(
                    onScanClick = { navController.navigate(Routes.SCAN) },
                    onSeeAllClick = { navController.goToTab(Routes.REPORTS, Routes.HOME) },
                    onReportClick = { navController.navigate(Routes.reportDetail(it)) }
                )
            }
            composable(Routes.REPORTS) {
                ReportsScreen(onReportClick = { navController.navigate(Routes.reportDetail(it)) })
            }
            composable(Routes.MAP) {
                MapScreen(onReportClick = { navController.navigate(Routes.reportDetail(it)) })
            }
            composable(Routes.PROFILE) {
                ProfileScreen(onAdminAccess = { switchRole(Role.ADMIN) })
            }
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
                    onSubmitted = { navController.goToTab(Routes.REPORTS, Routes.HOME) }
                )
            }

            // ---- Shared ----
            composable(
                route = Routes.REPORT_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                ReportDetailScreen(
                    isAdmin = role == Role.ADMIN,
                    reportId = entry.arguments?.getInt("id") ?: -1,
                    onBack = { navController.popBackStack() }
                )
            }

            // ---- Admin ----
            composable(Routes.ADMIN_DASH) {
                AdminDashboardScreen(
                    onReportClick = { navController.navigate(Routes.reportDetail(it)) },
                    onSeeAllClick = { navController.goToTab(Routes.ADMIN_REPORTS, Routes.ADMIN_DASH) }
                )
            }
            composable(Routes.ADMIN_REPORTS) {
                AdminReportsScreen(onReportClick = { navController.navigate(Routes.reportDetail(it)) })
            }
            composable(Routes.ADMIN_MAP) {
                MapScreen(onReportClick = { navController.navigate(Routes.reportDetail(it)) })
            }
            composable(Routes.ADMIN_ACCOUNT) {
                AdminAccountScreen(onExit = { switchRole(Role.CITIZEN) })
            }
        }
    }
}