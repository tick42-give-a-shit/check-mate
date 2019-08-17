package com.example.checkmate.api

import android.accounts.AccountManager
import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class ApiHeadersInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val original = chain.request()

        return chain.proceed(original)
    }
}