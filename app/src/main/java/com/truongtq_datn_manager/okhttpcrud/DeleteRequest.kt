package com.truongtq_datn_manager.okhttpcrud

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class DeleteRequest(private val url: String, private val jsonBody: String) {

    fun execute(isAuth: Boolean): Response? {
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .delete(body)
            .build()

        return try {
            val response = if (isAuth) {
                HttpClient.clientAuth.newCall(request).execute()
            } else {
                HttpClient.client.newCall(request).execute()
            }

            response
        } catch (e: IOException) {
            println("DELETE request failed: ${e.message}")
            null
        }
    }
}
