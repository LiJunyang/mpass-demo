package com.hsbcd.mpaastest.kotlin.samples.util

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.schedulers.Schedulers

fun <T> applySchedulers(): ObservableTransformer<T, T> {
    return ObservableTransformer<T, T> {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}