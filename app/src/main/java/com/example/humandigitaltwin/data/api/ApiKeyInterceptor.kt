package com.example.humandigitaltwin.data.api

import com.example.humandigitaltwin.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Content-Type", "application/json")

        if (BuildConfig.API_KEY.isNotBlank()) {
            builder.header("X-API-Key", BuildConfig.API_KEY)
        }

        val request = builder
            .method(original.method, original.body)
            .build()
        return chain.proceed(request)
    }
}
