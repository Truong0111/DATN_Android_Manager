package com.truongtq_datn_manager.okhttpcrud

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.util.concurrent.TimeUnit

object HttpClient {

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .sslSocketFactory(createInsecureSocketFactory(), createInsecureTrustManager())
        .hostnameVerifier { _, _ -> true }
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    val clientAuth: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .sslSocketFactory(createInsecureSocketFactory(), createInsecureTrustManager())
        .hostnameVerifier { _, _ -> true }
        .addInterceptor(AuthInterceptor())
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    private fun createInsecureSocketFactory(): javax.net.ssl.SSLSocketFactory {
        val trustAllCertificates = arrayOf<TrustManager>(createInsecureTrustManager())
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCertificates, java.security.SecureRandom())
        return sslContext.socketFactory
    }

    @SuppressLint("TrustAllX509TrustManager")
    private fun createInsecureTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        }
    }
}
