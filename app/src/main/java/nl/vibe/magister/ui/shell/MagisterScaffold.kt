package nl.vibe.magister.ui.shell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nl.vibe.magister.R
import nl.vibe.magister.ui.haptics.tick
import nl.vibe.magister.ui.haptics.rememberHaptics
import nl.vibe.magister.ui.navigation.Screen

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MagisterScaffold(
    title: String,
    current: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val haptics = rememberHaptics()

    Scaffold(
        modifier = modifier,
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(title) },
                actions = actions,
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = current == Screen.Home,
                    onClick = {
                        haptics.tick()
                        onNavigate(Screen.Home)
                    },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_agenda)) }
                )
                NavigationBarItem(
                    selected = current == Screen.Grades,
                    onClick = {
                        haptics.tick()
                        onNavigate(Screen.Grades)
                    },
                    icon = { Icon(Icons.Default.Grade, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_cijfers)) }
                )
                NavigationBarItem(
                    selected = current == Screen.Homework,
                    onClick = {
                        haptics.tick()
                        onNavigate(Screen.Homework)
                    },
                    icon = { Icon(Icons.Default.HomeWork, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_huiswerk)) }
                )
            }
        },
        floatingActionButton = floatingActionButton,
        content = content
    )
}
