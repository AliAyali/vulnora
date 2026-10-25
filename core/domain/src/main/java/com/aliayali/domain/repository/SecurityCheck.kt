package com.aliayali.domain.repository

import com.aliayali.model.http.HttpResponseData
import com.aliayali.model.scanner.CheckResult
import com.aliayali.model.scanner.ScanTarget

interface SecurityCheck {

    val id: String

    suspend fun execute(
        target: ScanTarget,
        response: HttpResponseData,
    ): CheckResult
}