package com.aliayali.vulnora.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.aliayali.designsystem.icon.VulnoraIcons
import com.aliayali.home.navigation.HomeNavKey
import com.aliayali.vulnora.R
import com.aliayali.home.R as homeR

data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @param:StringRes val iconTextId: Int,
    @param:StringRes val titleTextId: Int,
)

val HOME = TopLevelNavItem(
    selectedIcon = VulnoraIcons.Home,
    unselectedIcon = VulnoraIcons.HomeOutlined,
    iconTextId = homeR.string.feature_home_title,
    titleTextId = R.string.app_name,
)

val TOP_LEVEL_NAV_ITEMS = mapOf(
    HomeNavKey to HOME,
)