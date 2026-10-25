package com.aliayali.scanner.paths

import android.content.Context
import android.net.Uri
import com.aliayali.model.ScanProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommonPaths @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun getPaths(): List<String> = getPaths(ScanProfile.COMMON)

    fun getPaths(profile: ScanProfile): List<String> {
        val paths = try {
            context.assets.open(profile.assetName)
                .bufferedReader()
                .useLines { lines ->
                    lines.map(String::trim)
                        .filter(String::isNotEmpty)
                        .filterNot { it.startsWith("#") }
                        .filter(::isSafeRelativePath)
                        .distinctBy { it.lowercase().trimEnd('/') }
                        .toList()
                }
        } catch (exception: IOException) {
            throw IllegalStateException(
                "Unable to load scanner wordlist: assets/${profile.assetName}",
                exception,
            )
        }

        check(paths.isNotEmpty()) {
            "Scanner wordlist assets/${profile.assetName} is empty or contains no valid paths."
        }

        return paths
    }

    private fun isSafeRelativePath(path: String): Boolean {
        if (path.isBlank() || path.length > 220) return false
        if (path.startsWith('/') || path.startsWith('\\')) return false
        if ('\\' in path || '?' in path || '#' in path) return false
        if ("://" in path || "//" in path) return false
        if (path.any { it.isISOControl() }) return false
        if (Regex("""(?i)%(?:2e|2f|5c|00|3f|23)""").containsMatchIn(path)) return false

        var decoded = path
        repeat(3) {
            decoded = Uri.decode(decoded)
        }

        if (decoded.startsWith('/') || decoded.startsWith('\\')) return false
        if ('\\' in decoded || '?' in decoded || '#' in decoded) return false
        if ("://" in decoded || "//" in decoded) return false
        if (decoded.any { it.isISOControl() || it == '\uFFFD' }) return false
        if (decoded.split('/').any { it.trim() == "." || it.trim().startsWith("..") }) return false

        return true
    }
}
