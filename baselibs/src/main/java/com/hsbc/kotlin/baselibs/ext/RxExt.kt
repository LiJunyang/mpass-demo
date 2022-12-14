package com.hsbc.kotlin.baselibs.ext

import com.hsbc.kotlin.baselibs.bean.BaseBean
import com.hsbc.kotlin.baselibs.http.HttpStatus
import com.hsbc.kotlin.baselibs.http.exception.ExceptionHandle
import com.hsbc.kotlin.baselibs.http.function.RetryWithDelay
import com.hsbc.kotlin.baselibs.mvp.IModel
import com.hsbc.kotlin.baselibs.mvp.IView
import cert.hsbcsd.mpaastest.Rx.SchedulerUtils
import com.hsbc.kotlin.baselibs.utils.NetWorkUtil
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable

fun <T : BaseBean> Observable<T>.ss(
    model: IModel?,
    view: IView?,
    isShowLoading: Boolean = true,
    onSuccess: (T) -> Unit,
    onError: ((T) -> Unit)? = null
) {
    this.compose(SchedulerUtils.ioToMain())
        .retryWhen(RetryWithDelay())
        .subscribe(object : Observer<T> {
            override fun onComplete() {
                view?.hideLoading()
            }

            override fun onSubscribe(d: Disposable) {
                if (isShowLoading) view?.showLoading()
                model?.addDisposable(d)
                if (!NetWorkUtil.isConnected()) {
                    view?.showDefaultMsg("当前网络不可用，请检查网络设置")
                    d.dispose()
                    onComplete()
                }
            }

            override fun onNext(t: T) {
                view?.hideLoading()
                when {
                    t.errorCode == HttpStatus.SUCCESS -> onSuccess.invoke(t)
                    t.errorCode == HttpStatus.TOKEN_INVALID -> {
                        // Token 过期，重新登录
                    }
                    else -> {
                        if (onError != null) {
                            onError.invoke(t)
                        } else {
                            if (t.errorMsg.isNotEmpty())
                                view?.showDefaultMsg(t.errorMsg)
                        }
                    }
                }
            }

            override fun onError(t: Throwable) {
                view?.hideLoading()
                view?.showError(ExceptionHandle.handleException(t))
            }
        })
}

fun <T : BaseBean> Observable<T>.sss(
    view: IView?,
    isShowLoading: Boolean = true,
    onSuccess: (T) -> Unit,
    onError: ((T) -> Unit)? = null
): Disposable {
    if (isShowLoading) view?.showLoading()
    return this.compose(SchedulerUtils.ioToMain())
        .retryWhen(RetryWithDelay())
        .subscribe({
            if (isShowLoading) view?.showLoading()
            when {
                it.errorCode == HttpStatus.SUCCESS -> onSuccess.invoke(it)
                it.errorCode == HttpStatus.TOKEN_INVALID -> {
                    // Token 过期，重新登录
                }
                else -> {
                    if (onError != null) {
                        onError.invoke(it)
                    } else {
                        if (it.errorMsg.isNotEmpty())
                            view?.showDefaultMsg(it.errorMsg)
                    }
                }
            }
        }, {
            view?.hideLoading()
            view?.showError(ExceptionHandle.handleException(it))
        })
}
