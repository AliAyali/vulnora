package com.aliayali.scanner

import com.aliayali.domain.repository.SecurityCheck
import com.aliayali.model.http.HttpResponseData
import com.aliayali.model.scanner.CheckResult
import com.aliayali.model.scanner.CheckSeverity
import com.aliayali.model.scanner.CheckStatus
import com.aliayali.model.scanner.ScanTarget

class DirectoryListingCheck : SecurityCheck {

    override val id: String = "directory-listing"

    override suspend fun execute(
        target: ScanTarget,
        response: HttpResponseData,
    ): CheckResult {
        val body = response.body.lowercase()

        val hasIndexTitle =
            "<title>index of " in body ||
                    "<title>directory listing for " in body

        val hasParentDirectory = "parent directory" in body

        val hasDirectoryEntries =
            ("<pre>" in body && ("../" in body || "href=\"" in body)) ||
                    ("<table" in body && "href=" in body)

        val looksLikeDirectoryListing =
            response.statusCode == 200 &&
                    (hasIndexTitle || (hasParentDirectory && hasDirectoryEntries))

        return if (looksLikeDirectoryListing) {
            CheckResult(
                checkId = id,
                title = "Possible directory listing",
                description =
                    "The response contains indicators of an automatically " +
                            "generated directory index.",
                status = CheckStatus.FINDING,
                severity = CheckSeverity.MEDIUM,
                evidence =
                    "HTTP ${response.statusCode}; directory-index indicators detected.",
                recommendation =
                    "Verify whether directory indexing is intended. " +
                            "Disable it if files should not be publicly listed.",
            )
        } else {
            CheckResult(
                checkId = id,
                title = "Directory listing indicators",
                description =
                    "No obvious directory-index indicators were detected " +
                            "in the checked response.",
                status = CheckStatus.PASSED,
                severity = CheckSeverity.INFO,
            )
        }
    }
}