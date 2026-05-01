package com.michael.insightlyspend.presentation.navigation

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.michael.insightlyspend.presentation.analytics.AnalyticsScreen
import com.michael.insightlyspend.presentation.budget.BudgetScreen
import com.michael.insightlyspend.presentation.dashboard.DashboardScreen
import com.michael.insightlyspend.presentation.ledger.LedgerScreen
import com.michael.insightlyspend.presentation.receipts.ReceiptsScreen
import com.michael.insightlyspend.presentation.settings.SettingsScreen

@Composable
fun MainShell() {
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity

    val deepLinkTarget = activity.intent?.takeIf {
        it.action == Intent.ACTION_VIEW && it.data != null
    }?.data?.toString()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppRoutes.entries.forEach { route ->
                    NavigationBarItem(
                        selected = current?.hierarchy?.any { it.route == route.route } == true,
                        onClick = {
                            navController.navigate(route.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                route.icon,
                                contentDescription = stringResource(route.labelRes),
                            )
                        },
                        label = { Text(stringResource(route.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            NavHost(
                navController = navController,
                startDestination = AppRoutes.Dashboard.route,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(
                    route = AppRoutes.Dashboard.route,
                    deepLinks = deepLinksFor(AppRoutes.Dashboard),
                ) {
                    DashboardScreen()
                }
                composable(
                    route = AppRoutes.Ledger.route,
                    deepLinks = deepLinksFor(AppRoutes.Ledger),
                ) {
                    LedgerScreen()
                }
                composable(
                    route = AppRoutes.Analytics.route,
                    deepLinks = deepLinksFor(AppRoutes.Analytics),
                ) {
                    AnalyticsScreen()
                }
                composable(
                    route = AppRoutes.Budget.route,
                    deepLinks = deepLinksFor(AppRoutes.Budget),
                ) {
                    BudgetScreen()
                }
                composable(
                    route = AppRoutes.Receipts.route,
                    deepLinks = deepLinksFor(AppRoutes.Receipts),
                ) {
                    ReceiptsScreen()
                }
                composable(
                    route = AppRoutes.Settings.route,
                    deepLinks = deepLinksFor(AppRoutes.Settings),
                ) {
                    SettingsScreen()
                }
            }

            // Run after NavHost attaches the graph; handleDeepLink alone often stays on startDestination.
            LaunchedEffect(deepLinkTarget) {
                val route = routeFromInsightlyNavIntent(activity.intent) ?: return@LaunchedEffect
                delay(50)
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }
}

private fun routeFromInsightlyNavIntent(intent: Intent): String? {
    if (intent.action != Intent.ACTION_VIEW) return null
    val uri = intent.data ?: return null
    if (!uri.scheme.equals("insightlyspend", ignoreCase = true)) return null
    if (!uri.host.equals("nav", ignoreCase = true)) return null
    val segment = uri.pathSegments.firstOrNull() ?: return null
    return AppRoutes.entries.find { it.route == segment }?.route
}

private fun deepLinksFor(route: AppRoutes) = listOf(
    navDeepLink {
        uriPattern = "insightlyspend://nav/${route.route}"
    },
)
