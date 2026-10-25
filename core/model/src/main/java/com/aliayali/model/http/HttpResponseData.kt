package com.aliayali.model.http

data class HttpResponseData(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: String,
    val finalUrl: String,
)