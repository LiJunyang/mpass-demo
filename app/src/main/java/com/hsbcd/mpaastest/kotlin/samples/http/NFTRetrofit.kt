package com.hsbcd.mpaastest.kotlin.samples.http

import com.hsbc.kotlin.baselibs.http.RetrofitFactory

object NFTRetrofit : RetrofitFactory<NFTApi>() {

    const val BASE_URL = "http://mgw.mpaas.cn-hangzhou.aliyuncs.com"

    override fun baseUrl(): String = BASE_URL

    override fun getServiceType(): Class<NFTApi> = NFTApi::class.java

}