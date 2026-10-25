package com.aliayali.model

data class ScanResult(
    val scanId: String,
    val targetUrl: String,
    val status: ScanStatus,
    val findings: List<SecurityFinding> = emptyList(),
    val errorMessage: String? = null,
)