package com.aliayali.scanner.repository

import com.aliayali.domain.repository.ScanRepository
import com.aliayali.model.FindingSeverity
import com.aliayali.model.ScanRequest
import com.aliayali.model.ScanResult
import com.aliayali.model.ScanStatus
import com.aliayali.model.SecurityFinding
import com.aliayali.model.scanner.CheckSeverity
import com.aliayali.model.scanner.CheckStatus
import com.aliayali.model.scanner.ScanTarget
import com.aliayali.scanner.ScannerEngine
import kotlinx.coroutines.CancellationException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepositoryImpl @Inject constructor(
    private val scannerEngine: ScannerEngine,
) : ScanRepository {
    override suspend fun startScan(
        request: ScanRequest,
        onProgress: suspend (com.aliayali.model.ScanProgress) -> Unit,
    ): ScanResult {
        return try {
            val report = scannerEngine.scan(
                target = ScanTarget(
                    url = request.url,
                    profile = request.profile,
                ),
                onProgress = onProgress,
            )

            val checkFindings = report.results
                .filter { it.status == CheckStatus.FINDING }
                .map { result ->
                    SecurityFinding(
                        id = result.checkId,
                        title = result.title,
                        description = result.description,
                        affectedUrl = report.targetUrl,
                        severity = result.severity.toFindingSeverity(),
                        evidence = result.evidence,
                        recommendation = result.recommendation,
                    )
                }

            ScanResult(
                scanId = UUID.randomUUID().toString(),
                targetUrl = report.targetUrl,
                status = ScanStatus.COMPLETED,
                findings = (report.findings + checkFindings).distinctBy {
                    "${it.id}:${it.affectedUrl}"
                },
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            ScanResult(
                scanId = UUID.randomUUID().toString(),
                targetUrl = request.url,
                status = ScanStatus.FAILED,
                errorMessage = exception.message
                    ?: "Scan failed (${exception.javaClass.simpleName}).",
            )
        }
    }

    private fun CheckSeverity.toFindingSeverity(): FindingSeverity = when (this) {
        CheckSeverity.INFO -> FindingSeverity.INFO
        CheckSeverity.LOW -> FindingSeverity.LOW
        CheckSeverity.MEDIUM -> FindingSeverity.MEDIUM
        CheckSeverity.HIGH -> FindingSeverity.HIGH
    }
}
