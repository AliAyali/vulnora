package com.aliayali.vulnora.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.aliayali.home.navigation.homeEntry
import com.aliayali.navigation.Navigator
import com.aliayali.navigation.toEntries


@Composable
fun VulnoraApp(
    appState: VulnoraAppState,
    modifier: Modifier = Modifier,
) {
    val navigator = remember(appState.navigationState) {
        Navigator(appState.navigationState)
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            val entryProvider = entryProvider {
                homeEntry(navigator)
            }

            NavDisplay(
                entries = appState.navigationState.toEntries(entryProvider),
                onBack = {
                    navigator.goBack()
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}