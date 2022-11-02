package com.hsbc.kotlin.baselibs.mvp

import io.reactivex.rxjava3.disposables.Disposable

interface IModel {

    fun addDisposable(disposable: Disposable?)

    fun onDetach()

}