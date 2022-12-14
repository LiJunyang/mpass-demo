package com.hsbcd.mpaastest.kotlin.samples.ui.activity

import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.alibaba.android.arouter.launcher.ARouter
import com.hsbcd.mpaastest.kotlin.samples.constants.ARouterPath
import com.hsbcd.mpaastest.kotlin.samples.constants.Constant.WX_UNION_ID
import com.hsbcd.mpaastest.kotlin.samples.util.getUnionID
import com.hsbcd.mpaastest.kotlin.samples.util.isLogin
import com.hsbcd.mpaastest.kotlin.samples.util.isRegistered

class RegisterNavigationCallbackImpl: NavigationCallback {
    override fun onFound(postcard: Postcard?) {
    }

    override fun onLost(postcard: Postcard?) {
    }

    override fun onArrival(postcard: Postcard?) {
    }

    override fun onInterrupt(postcard: Postcard) {
        if (!isRegistered()){
            val bundle = postcard.extras
            ARouter.getInstance().build(ARouterPath.register)
                .with(bundle)
                .navigation()
        }else{
            if (!isLogin()){
                val bundle = postcard.extras
                bundle.putString(WX_UNION_ID, getUnionID())
                ARouter.getInstance().build(ARouterPath.login)
                    .with(bundle)
                    .navigation()
            }
        }
    }
}