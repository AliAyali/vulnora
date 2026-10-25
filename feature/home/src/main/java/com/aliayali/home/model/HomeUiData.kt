package com.aliayali.home.model

import com.aliayali.model.ScanProfile
import com.aliayali.model.SecurityFinding

data class HomeUiData(
    val url: String = "",
    val profile: ScanProfile = ScanProfile.COMMON,
    val isScanning: Boolean = false,
    val progress: Float = 0f,
    val testedCount: Int = 0,
    val totalCount: Int = 0,
    val testedPaths: List<TestedPathUi> = emptyList(),
    val securityFindings: List<SecurityFinding> = emptyList(),
    val findingsCount: Int = 0,
    val errorMessage: String? = null,
    val scanCompleted: Boolean = false,
)

data class TestedPathUi(
    val url: String,
    val statusCode: Int? = null,
    val isFinding: Boolean = false,
    val isTarget: Boolean = false,
)
