package com.hsbcd.mpaastest.kotlin.samples.http

import com.hsbc.kotlin.baselibs.http.RetrofitFactory
import com.hsbcd.mpaastest.kotlin.samples.constants.Constant

object MainRetrofit : RetrofitFactory<MainApi>() {

    override fun baseUrl(): String = Constant.BASE_URL

    override fun getServiceType(): Class<MainApi> = MainApi::class.java

}