package com.aliayali.model.scanner

import com.aliayali.model.ScanProfile

data class ScanTarget(
    val url: String,
    val profile: ScanProfile = ScanProfile.COMMON,
)