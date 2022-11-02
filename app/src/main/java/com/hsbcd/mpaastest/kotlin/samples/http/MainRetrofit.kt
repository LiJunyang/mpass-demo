package com.hsbcd.mpaastest.kotlin.samples.http

import com.hsbc.kotlin.baselibs.http.RetrofitFactory
import com.hsbcd.mpaastest.kotlin.samples.constant.Constant
import com.hsbcd.mpaastest.kotlin.samples.http.MainApi

object MainRetrofit : RetrofitFactory<MainApi>() {

    override fun baseUrl(): String = Constant.BASE_URL

    override fun getService(): Class<MainApi> = MainApi::class.java

}