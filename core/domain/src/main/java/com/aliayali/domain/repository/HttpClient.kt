package com.aliayali.domain.repository

import com.aliayali.model.http.HttpResponseData

interface HttpClient {

    suspend fun get(
        url: String,
    ): HttpResponseData
}