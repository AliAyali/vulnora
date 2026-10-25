package com.aliayali.scanner

import com.aliayali.domain.repository.HttpClient
import com.aliayali.domain.repository.SecurityCheck
import com.aliayali.model.FindingSeverity
import com.aliayali.model.ScanProgress
import com.aliayali.model.SecurityFinding
import com.aliayali.model.http.HttpResponseData
import com.aliayali.model.scanner.CheckResult
import com.aliayali.model.scanner.CheckSeverity
import com.aliayali.model.scanner.CheckStatus
import com.aliayali.model.scanner.ScanTarget
import com.aliayali.model.scanner.ScannerReport
import com.aliayali.scanner.paths.CommonPaths
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.URI
import java.util.Collections
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScannerEngine @Inject constructor(
    private val httpClient: HttpClient,
    private val checks: List<SecurityCheck>,
    private val commonPaths: CommonPaths,
) {
    suspend fun scan(
        target: ScanTarget,
        onProgress: suspend (ScanProgress) -> Unit,
    ): ScannerReport = withContext(Dispatchers.IO) {

        val normalizedUrl = normalizeTargetUrl(target.url)
        val normalizedTarget = target.copy(url = normalizedUrl)

        // Baseline response: used to compare candidate paths against the main page.
        val initialResponse = httpClient.get(normalizedUrl)

        val checkResults = checks.map { check ->
            try {
                check.execute(normalizedTarget, initialResponse)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                CheckResult(
                    checkId = check.id,
                    title = "Check could not complete",
                    description = "The check failed and did not produce a security conclusion.",
                    status = CheckStatus.ERROR,
                    severity = CheckSeverity.INFO,
                    evidence = exception.message?.take(200),
                )
            }
        }

        val paths = commonPaths.getPaths(target.profile)
        val total = paths.size

        val completed = AtomicInteger(0)
        val nextIndex = AtomicInteger(0)
        val findings = Collections.synchronizedList(
            mutableListOf<SecurityFinding>()
        )

        onProgress(
            ScanProgress(
                url = normalizedUrl,
                statusCode = initialResponse.statusCode,
                isFinding = false,
                testedCount = 0,
                totalCount = total,
            )
        )

        coroutineScope {
            val workerCount = minOf(MAX_CONCURRENT_REQUESTS, paths.size)

            List(workerCount) {
                async {
                    while (true) {
                        val index = nextIndex.getAndIncrement()
                        if (index >= paths.size) break

                        val path = paths[index]
                        val pathUrl = buildPathUrl(normalizedUrl, path)

                        val response = try {
                            httpClient.get(pathUrl)
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (_: Exception) {
                            null
                        }

                        // Category 1:
                        // Confirmed finding only when recognizable evidence exists.
                        val confirmedFinding = response?.let {
                            detectConfirmedExposure(
                                path = path,
                                url = pathUrl,
                                response = it,
                                baseline = initialResponse,
                            )
                        }

                        // Category 2:
                        // HTTP 200 without enough evidence becomes Needs review.
                        // It is displayed, but is not counted as a confirmed vulnerability.
                        val reviewFinding = if (
                            confirmedFinding == null &&
                            response?.statusCode == 200
                        ) {
                            createNeedsReviewFinding(
                                url = pathUrl,
                                response = response,
                                baseline = initialResponse,
                            )
                        } else {
                            null
                        }

                        confirmedFinding?.let { findings.add(it) }
                        reviewFinding?.let { findings.add(it) }

                        val count = completed.incrementAndGet()

                        onProgress(
                            ScanProgress(
                                url = pathUrl,
                                statusCode = response?.statusCode,
                                // Only confirmed vulnerabilities are flagged here.
                                isFinding = confirmedFinding != null,
                                testedCount = count,
                                totalCount = total,
                            )
                        )
                    }
                }
            }.awaitAll()
        }

        ScannerReport(
            targetUrl = normalizedUrl,
            results = checkResults,
            findings = findings.toList(),
        )
    }

    /**
     * Creates an informational finding for an HTTP 200 response that
     * does not match any of the confirmed-exposure signatures.
     */
    private fun createNeedsReviewFinding(
        url: String,
        response: HttpResponseData,
        baseline: HttpResponseData,
    ): SecurityFinding {
        val body = response.body

        val evidence = when {
            body.isBlank() -> {
                "HTTP 200 was returned, but the response body is empty."
            }

            samePageFingerprint(body, baseline.body) -> {
                "HTTP 200 was returned, but the response body closely matches " +
                        "the site's base page. This may be a soft 404 or fallback page."
            }

            looksLikeNotFound(body) -> {
                "HTTP 200 was returned, but the response contains common " +
                        "not-found wording. This may be a soft 404."
            }

            else -> {
                "HTTP 200 was returned without a recognized sensitive-content " +
                        "signature. The path may be valid, or it may return a generic application page."
            }
        }

        return SecurityFinding(
            id = "http-200-needs-review",
            title = "HTTP 200 — Needs review",
            description = "The server returned a successful status, but the response " +
                    "does not contain enough evidence to confirm a security exposure. " +
                    "Review the response manually. This is not counted as a confirmed vulnerability.",
            affectedUrl = url,
            severity = FindingSeverity.INFO,
            evidence = evidence,
            recommendation = "Inspect the response content and compare it with a " +
                    "known missing-path response. Only classify it as an exposure if " +
                    "sensitive file contents or other clear evidence are present.",
        )
    }

    private fun normalizeTargetUrl(value: String): String {
        val input = value.trim()
        val uri = URI(input)

        require(
            uri.scheme.equals("http", ignoreCase = true) ||
                    uri.scheme.equals("https", ignoreCase = true)
        ) {
            "Only HTTP and HTTPS target URLs are supported."
        }

        require(!uri.host.isNullOrBlank()) {
            "The target URL does not contain a valid host."
        }

        require(uri.rawUserInfo == null) {
            "URLs containing user information are not supported."
        }

        require(uri.rawQuery == null && uri.rawFragment == null) {
            "Enter a base URL without a query string or fragment."
        }

        return uri.normalize()
            .toASCIIString()
            .trimEnd('/')
            .ifBlank { input }
    }

    private fun buildPathUrl(
        baseUrl: String,
        path: String,
    ): String = "$baseUrl/${path.trimStart('/')}"

    /**
     * Returns a finding only when the HTTP 200 response contains
     * recognizable evidence of a sensitive file or exposure.
     */
    private fun detectConfirmedExposure(
        path: String,
        url: String,
        response: HttpResponseData,
        baseline: HttpResponseData,
    ): SecurityFinding? {
        if (response.statusCode != 200) return null

        val body = response.body

        if (body.isBlank()) return null
        if (looksLikeNotFound(body)) return null
        if (samePageFingerprint(body, baseline.body)) return null

        val normalizedPath = path.lowercase(Locale.ROOT).trimStart('/')
        val lower = body.lowercase(Locale.ROOT)

        // .env exposure
        if (
            isEnvironmentFile(normalizedPath) &&
            hasRealEnvironmentValues(body)
        ) {
            return SecurityFinding(
                id = "exposure-env-file",
                title = "Environment file appears publicly readable",
                description = "The response contains environment-style secret assignments in a public path.",
                affectedUrl = url,
                severity = FindingSeverity.CRITICAL,
                evidence = "Sensitive environment variable names and assigned values were detected. Values are not displayed.",
                recommendation = "Remove the file from the public web root, rotate any exposed credentials, and review server access rules.",
            )
        }

        // .git/config exposure
        if (
            normalizedPath.endsWith(".git/config") &&
            "[core]" in lower &&
            "repositoryformatversion" in lower
        ) {
            return SecurityFinding(
                id = "exposure-git-config",
                title = "Git repository configuration is publicly readable",
                description = "The response matches a Git repository configuration file.",
                affectedUrl = url,
                severity = FindingSeverity.HIGH,
                evidence = "Git configuration markers were detected in the response.",
                recommendation = "Block access to repository metadata and remove version-control data from the public web root.",
            )
        }

        // .git/HEAD exposure
        if (
            normalizedPath.endsWith(".git/head") &&
            Regex(
                """(?im)^\s*(?:ref:\s+refs/heads/|[a-f0-9]{40})\s*$"""
            ).containsMatchIn(body)
        ) {
            return SecurityFinding(
                id = "exposure-git-head",
                title = "Git repository metadata is publicly readable",
                description = "The response matches a Git HEAD file and may expose repository structure.",
                affectedUrl = url,
                severity = FindingSeverity.MEDIUM,
                evidence = "A Git HEAD reference or object identifier was detected.",
                recommendation = "Block public access to .git metadata and verify that repository objects are not exposed.",
            )
        }

        // WordPress configuration exposure
        if (
            isWpConfigPath(normalizedPath) &&
            Regex(
                """(?is)define\s*\(\s*['"]DB_(?:NAME|USER|PASSWORD)['"]"""
            ).containsMatchIn(body) &&
            Regex(
                """(?is)define\s*\(\s*['"]DB_PASSWORD['"]\s*,\s*['"][^'"]{2,}['"]"""
            ).containsMatchIn(body)
        ) {
            return SecurityFinding(
                id = "exposure-wordpress-config",
                title = "WordPress configuration appears exposed",
                description = "The response contains WordPress database configuration declarations and a non-empty password value.",
                affectedUrl = url,
                severity = FindingSeverity.CRITICAL,
                evidence = "WordPress database configuration markers were detected. Secret values are not displayed.",
                recommendation = "Restrict access to configuration files, rotate database credentials, and confirm that PHP files are executed by the server.",
            )
        }

        // SQL/database dump exposure
        if (
            isDatabaseDumpPath(normalizedPath) &&
            "create table" in lower &&
            (
                    "insert into" in lower ||
                            "mysqldump" in lower ||
                            "copy " in lower
                    )
        ) {
            return SecurityFinding(
                id = "exposure-database-dump",
                title = "Database dump appears publicly readable",
                description = "The response contains database-dump structure and row data markers.",
                affectedUrl = url,
                severity = FindingSeverity.CRITICAL,
                evidence = "SQL dump markers were detected in the response.",
                recommendation = "Remove database dumps from public storage, restrict access, and rotate credentials if sensitive data may have been disclosed.",
            )
        }

        // Private-key exposure
        if (
            isPrivateKeyPath(normalizedPath) &&
            (
                    "-----begin private key-----" in lower ||
                            "-----begin rsa private key-----" in lower ||
                            "-----begin ec private key-----" in lower
                    )
        ) {
            return SecurityFinding(
                id = "exposure-private-key",
                title = "Private key file appears publicly readable",
                description = "The response contains a private-key PEM header.",
                affectedUrl = url,
                severity = FindingSeverity.CRITICAL,
                evidence = "A private-key header was detected. Key material is not displayed.",
                recommendation = "Remove the key from public paths and revoke or rotate the exposed key pair immediately.",
            )
        }

        // PHP information-page exposure
        if (
            (
                    normalizedPath.contains("phpinfo") ||
                            normalizedPath.endsWith("/info.php")
                    ) &&
            "php version" in lower &&
            "configuration file (php.ini) path" in lower
        ) {
            return SecurityFinding(
                id = "exposure-phpinfo",
                title = "PHP configuration information is publicly exposed",
                description = "The response matches a PHP information page that reveals server configuration details.",
                affectedUrl = url,
                severity = FindingSeverity.MEDIUM,
                evidence = "PHP information-page markers were detected.",
                recommendation = "Remove diagnostic pages from production and restrict access to runtime information.",
            )
        }

        // Directory listing exposure
        if (looksLikeDirectoryListing(body)) {
            return SecurityFinding(
                id = "exposure-directory-listing",
                title = "Directory listing is enabled",
                description = "The response appears to list files from a directory rather than serving an application page.",
                affectedUrl = url,
                severity = FindingSeverity.MEDIUM,
                evidence = "Directory-index heading and entry markers were detected.",
                recommendation = "Disable directory indexing or ensure the directory contains only intentionally public files.",
            )
        }

        // .htpasswd exposure
        if (
            normalizedPath.endsWith(".htpasswd") &&
            Regex(
                """(?m)^\s*[^:#\s]+:(?:[$]apr1[$]|[$]2[aby][$]|[$]6[$]|[$]5[$]|\{SHA\})"""
            ).containsMatchIn(body)
        ) {
            return SecurityFinding(
                id = "exposure-password-file",
                title = "Password-hash file appears publicly readable",
                description = "The response contains the structure of an Apache password file.",
                affectedUrl = url,
                severity = FindingSeverity.CRITICAL,
                evidence = "Password-hash line patterns were detected; hash values are not displayed.",
                recommendation = "Block public access to password files and rotate affected credentials.",
            )
        }

        return null
    }

    private fun isEnvironmentFile(path: String): Boolean {
        val name = path.substringAfterLast('/')

        if (!name.startsWith(".env")) return false

        return name !in setOf(
            ".env.example",
            ".env.sample",
            ".env.dist",
            ".env.template",
        )
    }

    private fun hasRealEnvironmentValues(body: String): Boolean {
        val sensitiveKeys = listOf(
            "APP_KEY",
            "DB_PASSWORD",
            "DATABASE_URL",
            "AWS_SECRET_ACCESS_KEY",
            "SECRET_KEY",
            "JWT_SECRET",
            "STRIPE_SECRET_KEY",
            "MAIL_PASSWORD",
            "REDIS_PASSWORD",
            "PRIVATE_KEY",
            "API_SECRET",
        )

        val assignedKeys = sensitiveKeys.count { key ->
            Regex(
                """(?im)^\s*${Regex.escape(key)}\s*=\s*\S{4,}"""
            ).containsMatchIn(body)
        }

        return assignedKeys >= 1 &&
                body.lineSequence().count { it.contains('=') } >= 2
    }

    private fun isWpConfigPath(path: String): Boolean {
        val name = path.substringAfterLast('/')

        return name.startsWith("wp-config.php") &&
                !name.contains("sample") &&
                !name.contains("example")
    }

    private fun isDatabaseDumpPath(path: String): Boolean =
        path.endsWith(".sql") ||
                path.endsWith(".dump") ||
                path.endsWith(".pgsql")

    private fun isPrivateKeyPath(path: String): Boolean =
        path.endsWith(".pem") ||
                path.endsWith(".key") ||
                path.substringAfterLast('/') in setOf(
            "id_rsa",
            "id_ed25519",
            "private.key",
            "server.key",
        )

    private fun looksLikeDirectoryListing(body: String): Boolean {
        val lower = body.lowercase(Locale.ROOT)

        val heading =
            "<title>index of " in lower ||
                    "<title>directory listing for " in lower

        val entries =
            ("<pre" in lower || "<table" in lower || "<ul" in lower) &&
                    "href=" in lower

        return heading && entries
    }

    private fun looksLikeNotFound(body: String): Boolean {
        val lower = body.lowercase(Locale.ROOT)

        return listOf(
            "page not found",
            "404 not found",
            "the requested url was not found",
            "the requested resource was not found",
            "no route matched",
            "we couldn't find that page",
            "we could not find that page",
            "this page doesn't exist",
            "this page does not exist",
        ).any { it in lower }
    }

    private fun samePageFingerprint(
        body: String,
        baseline: String,
    ): Boolean {
        if (body.isBlank() || baseline.isBlank()) return false

        val candidate = normalizePage(body)
        val original = normalizePage(baseline)

        if (candidate.length < 80 || original.length < 80) return false

        return candidate == original
    }

    private fun normalizePage(value: String): String = value
        .replace(Regex("""(?is)<script\b.*?</script>"""), " ")
        .replace(Regex("""(?is)<style\b.*?</style>"""), " ")
        .replace(Regex("""(?is)<[^>]+>"""), " ")
        .replace(Regex("""&(?:nbsp|amp|quot|lt|gt);"""), " ")
        .replace(Regex("""\s+"""), " ")
        .trim()
        .lowercase(Locale.ROOT)

    private companion object {
        const val MAX_CONCURRENT_REQUESTS = 2
    }
}