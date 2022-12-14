package com.hsbcd.mpaastest.kotlin.samples.http

import com.hsbcd.mpaastest.kotlin.samples.http.model.ciam.CIAMOAuthTokenRequest
import com.hsbcd.mpaastest.kotlin.samples.http.model.ciam.CIAMOAuthTokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface CIAMApi {
    @POST("api/bff/v1.2/developer/ciam/oauth/token")
fun getOAuthToken(@Body body: CIAMOAuthTokenRequest): CIAMOAuthTokenResponse
}