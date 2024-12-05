package com.truongtq_datn.okhttpcrud

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class PostRequest(private val url: String, private val jsonBody: String) {

    fun execute(isAuth: Boolean): Response? {
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = jsonBody.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        Log.d("API_Test", request.toString())

        return try {
            val response = if (isAuth) {
                HttpClient.clientAuth.newCall(request).execute()
            } else {
                HttpClient.client.newCall(request).execute()
            }

            response
        } catch (e: IOException) {
            println(e.message)
            Log.e("API_Test", "Error executing POST request: ${e.message}")
            null
        }
    }
}
