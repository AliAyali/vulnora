package com.aliayali.model.scanner

import com.aliayali.model.SecurityFinding

enum class CheckSeverity {
    INFO,
    LOW,
    MEDIUM,
    HIGH,
}

enum class CheckStatus {
    PASSED,
    FINDING,
    ERROR,
}

data class CheckResult(
    val checkId: String,
    val title: String,
    val description: String,
    val status: CheckStatus,
    val severity: CheckSeverity,
    val evidence: String? = null,
    val recommendation: String? = null,
)

data class ScannerReport(
    val targetUrl: String,
    val results: List<CheckResult>,
    val findings: List<SecurityFinding> = emptyList(),
)
