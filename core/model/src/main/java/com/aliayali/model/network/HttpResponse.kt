package com.aliayali.model.network

data class HttpResponse(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: String,
    val finalUrl: String,
)