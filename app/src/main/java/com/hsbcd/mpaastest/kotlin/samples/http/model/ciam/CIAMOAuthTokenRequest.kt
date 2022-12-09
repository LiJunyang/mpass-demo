package com.hsbcd.mpaastest.kotlin.samples.http.model.ciam

//client_id:9d454c637041ffca8006c99e4a4a1d3cfV3ajLpjKa5
//client_secret:TYceqtkDt5TQVOuvy1rvxyUeMI9IsQvUeC1y6Uqf2E
//grant_type:authorization_code
//scope:USER_API,openid
//code:c4446703d0ae08ab003b7b20bef4efbe
////redirect_uri:

data class CIAMOAuthTokenRequest(
    val client_id: String,
    val client_secret:String,
    val grant_type:String,
    val scope:String,
    val code:String
)