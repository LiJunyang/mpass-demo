package com.hsbcd.mpaastest.kotlin.samples.http

import com.hsbcd.mpaastest.kotlin.samples.http.model.nft.RegisterRequest
import com.hsbcd.mpaastest.kotlin.samples.http.model.nft.RegisterResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface NFTApi {
    @POST("api/user-registration")
    fun register(@Body body: RegisterRequest): Observable<RegisterResponse>
}