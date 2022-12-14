package com.hsbcd.mpaastest.kotlin.samples.http.model.nft

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val _requestBody : RequestBody,
)

@JsonClass(generateAdapter = true)
data class RequestBody(
    val userId : String,
    val nickName : String,
    val avatarUrl : String,
    val phoneNo : String = "",
    val email : String = "",
    val mark : String = "",
    val userName : String = ""
)