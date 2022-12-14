package com.hsbcd.mpaastest.kotlin.samples.util

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.schedulers.Schedulers
import java.security.MessageDigest

fun <T> applySchedulers(): ObservableTransformer<T, T> {
    return ObservableTransformer<T, T> {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun md5(content:String):String{
    val hash = MessageDigest.getInstance("MD5").digest(content.toByteArray())
    val hex = java.lang.StringBuilder(hash.size*2)
    for(b in hash){
        var str = Integer.toHexString(b.toInt())
        if (b < 0x10){
            str = "0$str"
        }
        hex.append(str.substring(str.length - 2))
    }
    return hex.toString()
}