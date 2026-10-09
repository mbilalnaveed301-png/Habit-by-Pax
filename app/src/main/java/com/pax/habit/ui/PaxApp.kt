@file:OptIn(ExperimentalMaterial3Api::class)

package com.pax.habit.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pax.habit.domain.AppState
import com.pax.habit.ui.components.PaxIcons
import com.pax.habit.ui.screens.AddEditHabitScreen
import com.pax.habit.ui.screens.DetailScreen
import com.pax.habit.ui.screens.HabitsScreen
import com.pax.habit.ui.screens.HomeScreen
import com.pax.habit.ui.screens.ProfileScreen
import com.pax.habit.ui.screens.StatsScreen
import com.pax.habit.ui.theme.PaxTheme

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val Tabs = listOf(
    Tab("home", "Home", PaxIcons.Home),
    Tab("habits", "Habits", PaxIcons.Habits),
    Tab("stats", "Statistics", PaxIcons.Stats),
    Tab("profile", "Profile", PaxIcons.Profile)
)

@Composable
fun PaxApp(vm: HabitsViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val dark = when (state.settings.theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }
    PaxTheme(darkTheme = dark, dynamicColor = state.settings.dynamicColor) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (state.loaded) PaxNav(vm, state) else Box(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun PaxNav(vm: HabitsViewModel, state: AppState) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.events.collect { snackbar.showSnackbar(it.message) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (route == null || Tabs.any { it.route == route }) {
                NavigationBar {
                    Tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = backStack?.destination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)
        ) {
            composable("home") {
                HomeScreen(
                    state = state,
                    onToggle = vm::toggleToday,
                    onOpenHabit = { nav.navigate("detail/$it") },
                    onAddHabit = { nav.navigate("edit/0") }
                )
            }
            composable("habits") {
                HabitsScreen(
                    state = state,
                    onOpenHabit = { nav.navigate("detail/$it") },
                    onAddHabit = { nav.navigate("edit/0") }
                )
            }
            composable("stats") { StatsScreen(state) }
            composable("profile") {
                ProfileScreen(
                    state = state,
                    onTheme = vm::setTheme,
                    onDynamicColor = vm::setDynamicColor,
                    onExport = vm::exportBackup,
                    onImport = vm::importBackup
                )
            }
            composable("edit/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                AddEditHabitScreen(
                    initial = state.habits.firstOrNull { it.habit.id == id }?.habit,
                    today = state.today,
                    onSave = {
                        vm.saveHabit(it)
                        nav.popBackStack()
                    },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                DetailScreen(
                    habitId = id,
                    state = state,
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate("edit/$id") },
                    onPause = { vm.pauseHabit(id) },
                    onResume = { vm.resumeHabit(id) },
                    onDelete = {
                        vm.deleteHabit(id)
                        nav.popBackStack()
                    }
                )
            }
        }
    }
}
