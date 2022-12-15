package com.hsbcd.mpaastest.kotlin.samples.ui.activity.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.hsbcsd.mpaastest.databinding.ActivitySplashBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.hsbcd.mpaastest.kotlin.samples.constants.ARouterPath
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.RegisterNavigationCallbackImpl

class SplashActivity: AppCompatActivity(){
    val binding:ActivitySplashBinding by lazy{
        ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        setContentView(binding.root)
        binding.root.postDelayed({
            ARouter.getInstance().build(ARouterPath.home)
            .navigation(this, RegisterNavigationCallbackImpl())
            finish()},1500)

    }
}