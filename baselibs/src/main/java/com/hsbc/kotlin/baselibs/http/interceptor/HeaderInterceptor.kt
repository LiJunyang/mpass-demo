package com.hsbc.kotlin.baselibs.http.interceptor

import com.hsbc.kotlin.baselibs.http.constant.HttpConstant
import com.hsbc.kotlin.baselibs.utils.Preference
import okhttp3.Interceptor
import okhttp3.Response

/**
 * @desc HeaderInterceptor: 设置请求头
 */
class HeaderInterceptor : Interceptor {

    /**
     * token
     */
    private var token: String by Preference(HttpConstant.TOKEN_KEY, "")

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val builder = request.newBuilder()

        builder.addHeader("Content-type", "application/json; charset=utf-8")
            .addHeader("AppId", "ALIPUBB1062A1011518")//mpaas gateway required
        // .method(request.method(), request.body())

        val domain = request.url.host
        val url = request.url.toString()
        if (domain.isNotEmpty()) {
            val spDomain: String by Preference(domain, "")
            val cookie: String = if (spDomain.isNotEmpty()) spDomain else ""
            if (cookie.isNotEmpty()) {
                // 将 Cookie 添加到请求头
                builder.addHeader(HttpConstant.COOKIE_NAME, cookie)
            }
        }

        return chain.proceed(builder.build())
    }

}