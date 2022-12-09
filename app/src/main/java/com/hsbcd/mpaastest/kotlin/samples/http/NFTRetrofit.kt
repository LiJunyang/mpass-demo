package com.hsbcd.mpaastest.kotlin.samples.http

import com.hsbc.kotlin.baselibs.http.RetrofitFactory

object NFTRetrofit : RetrofitFactory<NFTApi>() {

    const val BASE_URL = "http://api.hsbcsd.cn:8080"

    override fun baseUrl(): String = BASE_URL

    override fun getServiceType(): Class<NFTApi> = NFTApi::class.java

}