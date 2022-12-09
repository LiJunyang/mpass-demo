package com.hsbcd.mpaastest.kotlin.samples.http.model.nft

import com.squareup.moshi.JsonClass

//{
//    "userId": "o-uxWwVmXta7bMjEbHT69Qnnj3I8",
//    "nickName": "Lily Li",
//    "avatarUrl": "",
//    "phoneNo": "13289337131",
//    "email": "lily.li@example.com",
//    "mark": "test",
//    "userName": "Lily LI"
//}
@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val userId : String,
    val nickName : String,
    val avatarUrl : String,
    val phoneNo : String = "",
    val email : String = "",
    val mark : String = "",
    val userName : String = ""
)