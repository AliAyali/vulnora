package com.aliayali.model

enum class ScanProfile(val assetName: String) {
    COMMON("common_paths.txt"),
    WORDPRESS("wordpress_paths.txt"),
    DRUPAL("drupal_paths.txt"),
    JOOMLA("joomla_paths.txt"),
    API("api_paths.txt"),
    FRAMEWORK("framework_paths.txt"),
}

data class ScanRequest(
    val url: String,
    val profile: ScanProfile = ScanProfile.COMMON,
)
