package com.aliayali.model

data class SecurityFinding(
    val id: String,
    val title: String,
    val description: String,
    val affectedUrl: String,
    val severity: FindingSeverity,
    val evidence: String? = null,
    val recommendation: String? = null,
)