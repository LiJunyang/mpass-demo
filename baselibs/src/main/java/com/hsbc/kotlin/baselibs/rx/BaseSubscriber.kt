package cn.hsbcsd.mpaastest.Rx

import com.hsbc.kotlin.baselibs.bean.BaseBean
import com.hsbc.kotlin.baselibs.http.HttpStatus
import com.hsbc.kotlin.baselibs.http.exception.ExceptionHandle
import com.hsbc.kotlin.baselibs.mvp.IView
import com.hsbc.kotlin.baselibs.utils.NetWorkUtil
import io.reactivex.rxjava3.subscribers.ResourceSubscriber

abstract class BaseSubscriber<T : BaseBean> : ResourceSubscriber<T> {

    private var mView: IView? = null
    private var mErrorMsg = ""
    private var bShowLoading = true

    constructor(view: IView) {
        this.mView = view
    }

    constructor(view: IView, bShowLoading: Boolean) {
        this.mView = view
        this.bShowLoading = bShowLoading
    }

    /**
     * 成功的回调
     */
    protected abstract fun onSuccess(t: T)

    /**
     * 错误的回调
     */
    protected fun onError(t: T) {}

    override fun onStart() {
        super.onStart()
        if (bShowLoading) mView?.showLoading()
        if (!NetWorkUtil.isConnected()) {
            mView?.showDefaultMsg("当前网络不可用，请检查网络设置")
            onComplete()
        }
    }

    override fun onNext(t: T) {
        mView?.hideLoading()
        when {
            t.errorCode == HttpStatus.SUCCESS -> onSuccess(t)
            t.errorCode == HttpStatus.TOKEN_INVALID -> {
                // TODO Token 过期，重新登录
            }
            else -> {
                onError(t)
                if (t.errorMsg.isNotEmpty())
                    mView?.showDefaultMsg(t.errorMsg)
            }
        }
    }

    override fun onError(e: Throwable) {
        mView?.hideLoading()
        if (mView == null) {
            throw RuntimeException("mView can not be null")
        }
        if (mErrorMsg.isEmpty()) {
            mErrorMsg = ExceptionHandle.handleException(e)
        }
        mView?.showDefaultMsg(mErrorMsg)
    }

    override fun onComplete() {
        mView?.hideLoading()
    }

}