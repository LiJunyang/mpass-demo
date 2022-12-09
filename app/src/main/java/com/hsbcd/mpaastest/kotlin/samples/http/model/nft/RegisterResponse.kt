package com.hsbcd.mpaastest.kotlin.samples.http.model.nft

import com.squareup.moshi.JsonClass

//"success": false,
//"resultCode": "BIZ_ERROR",
//"resultMessage": "[{\"data\":{\"gmtModified\":1669885853840,\"avatarUrl\":\"\",\"nickName\":\"Lily Li\",\"tntInstId\":\"xdJ_B65q\",\"appId\":\"LeFIV12j6urqhARF\",\"userName\":\"Lily LI\",\"env\":\"PROD\",\"gmtCreate\":1669885853840,\"userId\":\"o-uxWwVmXta7bMjEbHT69Qnnj3I8\",\"phoneNo\":\"13289337131\",\"email\":\"lily.li@example.com\",\"mark\":\"test\"},\"error\":\"The user is registered.\"}]",
//"data": null
@JsonClass(generateAdapter = true)
data class RegisterResponse(
    val success:Boolean,
    val resultCode:String,
    val resultMessage:String,
    val data:String
)

