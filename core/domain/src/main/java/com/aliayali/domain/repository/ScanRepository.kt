package com.aliayali.domain.repository

import com.aliayali.model.ScanProgress
import com.aliayali.model.ScanRequest
import com.aliayali.model.ScanResult

interface ScanRepository {
    suspend fun startScan(
        request: ScanRequest,
        onProgress: suspend (ScanProgress) -> Unit = {},
    ): ScanResult
}
