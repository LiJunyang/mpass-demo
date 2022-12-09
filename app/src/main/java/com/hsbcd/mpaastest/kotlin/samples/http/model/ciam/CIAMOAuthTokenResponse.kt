package com.hsbcd.mpaastest.kotlin.samples.http.model.ciam

//{
//    "success": false,
//    "code": "Params.Illegal",
//    "message": "Params.Illegal",
//    "requestId": "1669823759798$b4ce2f95-4826-22e8-735b-c05ce3cc5be7",
//    "data": null
//}
data class CIAMOAuthTokenResponse(
    val success: Boolean,
    val code:String,
    val message:String,
    val requestId:String,
    val data:String
)

