package com.hsbcd.mpaastest.kotlin.samples.ui.activity

import android.content.Context
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor
import com.hsbcd.mpaastest.kotlin.samples.constants.ARouterPath
import com.hsbcd.mpaastest.kotlin.samples.util.isLogin
import com.hsbcd.mpaastest.kotlin.samples.util.isRegistered

@Interceptor(name = "register", priority = 1)
class RegisterInterceptorImpl:IInterceptor {
    override fun init(context: Context?) {
    }

    override fun process(postcard: Postcard, callback: InterceptorCallback) {
        when(postcard.path){
            ARouterPath.register, ARouterPath.login->callback.onContinue(postcard)
            else->{
                if (isRegistered() && isLogin()) {
                    callback.onContinue(postcard)
                } else {
                    callback.onInterrupt(null)
                }
            }
        }

    }
}