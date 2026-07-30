package com.bangersoul.aivance.job.base

import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", "Aivance/1.0.0 (Android)")
            .build()
        return chain.proceed(request)
    }
}
