package com.aliayali.network

import com.aliayali.domain.repository.HttpClient
import com.aliayali.model.http.HttpResponseData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import javax.inject.Inject

class OkHttpHttpClient @Inject constructor(
    private val client: okhttp3.OkHttpClient,
) : HttpClient {

    override suspend fun get(url: String): HttpResponseData =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .header("User-Agent", "Vulnora-SecurityScanner/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body

                val body = responseBody?.byteStream()?.use { input ->
                    val output = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var totalBytes = 0
                    val maxBytes = 512 * 1024

                    while (totalBytes < maxBytes) {
                        val remaining = maxBytes - totalBytes
                        val count = input.read(
                            buffer,
                            0,
                            minOf(buffer.size, remaining),
                        )

                        if (count == -1) break

                        output.write(buffer, 0, count)
                        totalBytes += count
                    }

                    String(
                        output.toByteArray(),
                        responseBody.contentType()?.charset(Charsets.UTF_8)
                            ?: Charsets.UTF_8,
                    )
                }.orEmpty()

                HttpResponseData(
                    statusCode = response.code,
                    headers = response.headers.toMultimap(),
                    body = body,
                    finalUrl = response.request.url.toString(),
                )
            }
        }
}