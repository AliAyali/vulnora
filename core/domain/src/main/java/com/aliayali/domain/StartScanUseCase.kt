package com.aliayali.domain

import com.aliayali.domain.repository.ScanRepository
import com.aliayali.model.ScanProfile
import com.aliayali.model.ScanProgress
import com.aliayali.model.ScanRequest
import com.aliayali.model.ScanResult
import javax.inject.Inject

class StartScanUseCase @Inject constructor(
    private val scanRepository: ScanRepository,
) {
    suspend operator fun invoke(
        url: String,
        profile: ScanProfile = ScanProfile.COMMON,
        onProgress: suspend (ScanProgress) -> Unit = {},
    ): ScanResult = scanRepository.startScan(
        request = ScanRequest(
            url = url,
            profile = profile,
        ),
        onProgress = onProgress,
    )
}