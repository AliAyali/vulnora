package com.aliayali.model.scanner

data class ScannerConfig(
    val connectTimeoutMillis: Long = 10_000,
    val readTimeoutMillis: Long = 10_000,
    val maxResponseBytes: Int = 512 * 1024,
)