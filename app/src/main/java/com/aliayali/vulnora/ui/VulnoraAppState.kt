package com.aliayali.vulnora.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.aliayali.home.navigation.HomeNavKey
import com.aliayali.navigation.NavigationState
import com.aliayali.navigation.rememberNavigationState
import com.aliayali.vulnora.navigation.TOP_LEVEL_NAV_ITEMS

class VulnoraAppState(
    val navigationState: NavigationState,
)

@Composable
fun rememberVulnoraAppState(): VulnoraAppState {

    val navigationState = rememberNavigationState(
        startKey = HomeNavKey,
        topLevelKeys = TOP_LEVEL_NAV_ITEMS.keys,
    )

    return remember {
        VulnoraAppState(
            navigationState = navigationState,
        )
    }
}