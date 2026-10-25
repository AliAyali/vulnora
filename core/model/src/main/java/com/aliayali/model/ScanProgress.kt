package com.aliayali.model

data class ScanProgress(
    val url: String,
    val statusCode: Int?,
    val isFinding: Boolean,
    val testedCount: Int,
    val totalCount: Int,
)
