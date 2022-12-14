package com.hsbcd.mpaastest.kotlin.samples.util

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.hsbc.kotlin.baselibs.config.AppConfig
import com.hsbcd.mpaastest.kotlin.samples.constants.Constant.WX_UNION_ID

private var loginLiveData = MutableLiveData<Boolean>(false)
fun isLogin():Boolean{
    return loginLiveData.value == true
}
fun setIsLogin(){
    loginLiveData.postValue(true)
}


private const val USER_SESSION_INFO_PREF = "user_session_info"

fun isRegistered():Boolean{
    val prefs = AppConfig.getApplication().getSharedPreferences(USER_SESSION_INFO_PREF, Context.MODE_PRIVATE)
    return prefs.getString(WX_UNION_ID, "")?.isNotEmpty()?:false
}

fun saveUnionID(unionId:String){
    val prefs = AppConfig.getApplication().getSharedPreferences(USER_SESSION_INFO_PREF, Context.MODE_PRIVATE)
    prefs.edit().putString(WX_UNION_ID,unionId).commit()
}

fun getUnionID():String?{
    val prefs = AppConfig.getApplication().getSharedPreferences(USER_SESSION_INFO_PREF, Context.MODE_PRIVATE)
    return prefs.getString(WX_UNION_ID,"")
}
