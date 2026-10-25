package com.aliayali.scanner

import com.aliayali.domain.repository.SecurityCheck
import com.aliayali.model.http.HttpResponseData
import com.aliayali.model.scanner.CheckResult
import com.aliayali.model.scanner.CheckSeverity
import com.aliayali.model.scanner.CheckStatus
import com.aliayali.model.scanner.ScanTarget

class SecurityHeadersCheck : SecurityCheck {

    override val id: String = "security-headers"

    override suspend fun execute(
        target: ScanTarget,
        response: HttpResponseData,
    ): CheckResult {
        val headerNames = response.headers.keys
            .map { it.lowercase() }
            .toSet()

        val missingHeaders = buildList {
            if ("x-content-type-options" !in headerNames) {
                add("X-Content-Type-Options")
            }

            if ("content-security-policy" !in headerNames) {
                add("Content-Security-Policy")
            }

            if (
                target.url.startsWith(
                    prefix = "https://",
                    ignoreCase = true,
                ) &&
                "strict-transport-security" !in headerNames
            ) {
                add("Strict-Transport-Security")
            }
        }

        if (missingHeaders.isEmpty()) {
            return CheckResult(
                checkId = id,
                title = "Security headers",
                description = "The checked security headers are present.",
                status = CheckStatus.PASSED,
                severity = CheckSeverity.INFO,
            )
        }

        return CheckResult(
            checkId = id,
            title = "Missing security headers",
            description =
                "Some recommended security headers were not found " +
                        "in the initial response.",
            status = CheckStatus.FINDING,
            severity = CheckSeverity.LOW,
            evidence = "Missing: ${missingHeaders.joinToString()}",
            recommendation =
                "Review the security-header policy for this website. " +
                        "Choose a Content-Security-Policy appropriate to its resources.",
        )
    }
}