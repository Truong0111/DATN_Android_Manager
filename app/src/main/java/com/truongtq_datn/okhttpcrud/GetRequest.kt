package com.truongtq_datn.okhttpcrud

import okhttp3.*
import java.io.IOException

class GetRequest(private val url: String) {

    fun execute(isAuth: Boolean): Response? {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return try {
            val response = if (isAuth) {
                HttpClient.clientAuth.newCall(request).execute()
            } else {
                HttpClient.client.newCall(request).execute()
            }

            response
        } catch (e: IOException) {
            println("GET request failed: ${e.message}")
            null
        }
    }
}
