package com.aliayali.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun VulnoraNavigationSuiteScaffold(
    navigationSuiteItems: VulnoraNavigationSuiteScope.() -> Unit,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
    content: @Composable () -> Unit,
) {
    val navigationSuiteItemColors = NavigationSuiteItemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = VulnoraNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = VulnoraNavigationDefaults.navigationContentColor(),
            selectedTextColor = VulnoraNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = VulnoraNavigationDefaults.navigationContentColor(),
            indicatorColor = VulnoraNavigationDefaults.navigationIndicatorColor(),
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = VulnoraNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = VulnoraNavigationDefaults.navigationContentColor(),
            selectedTextColor = VulnoraNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = VulnoraNavigationDefaults.navigationContentColor(),
            indicatorColor = VulnoraNavigationDefaults.navigationIndicatorColor(),
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
            selectedIconColor = VulnoraNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = VulnoraNavigationDefaults.navigationContentColor(),
            selectedTextColor = VulnoraNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = VulnoraNavigationDefaults.navigationContentColor(),
        ),
    )
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            VulnoraNavigationSuiteScope(
                navigationSuiteScope = this,
                navigationSuiteItemColors = navigationSuiteItemColors,
            ).run(navigationSuiteItems)
        },
        layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(windowAdaptiveInfo),
        modifier = modifier,
        content = content,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            navigationRailContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    )
}

class VulnoraNavigationSuiteScope internal constructor(
    private val navigationSuiteScope: NavigationSuiteScope,
    private val navigationSuiteItemColors: NavigationSuiteItemColors,
) {
    fun item(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: @Composable () -> Unit,
        selectedIcon: @Composable () -> Unit = icon,
        label: @Composable (() -> Unit)? = null,
    ) = navigationSuiteScope.item(
        selected = selected,
        onClick = onClick,
        icon = {
            if (selected) selectedIcon() else icon()
        },
        label = label,
        modifier = modifier,
        colors = navigationSuiteItemColors,
    )
}

object VulnoraNavigationDefaults {

    @Composable
    fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onBackground

    @Composable
    fun navigationIndicatorColor() = Color.Transparent
}