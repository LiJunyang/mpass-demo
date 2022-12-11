package com.hsbcd.mpaastest.kotlin.samples.ui.activity.register

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.com.hsbc.hsbcchina.cert.databinding.ActivityRegisterBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.hsbcd.mpaastest.kotlin.samples.constants.ARouterPath
import com.hsbcd.mpaastest.kotlin.samples.constants.Constant.WX_UNION_ID
import com.hsbcd.mpaastest.kotlin.samples.constants.WeChat.WX_APP_ID
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.login.LoginActivity
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

@Route(path = ARouterPath.register)
class RegisterActivity: AppCompatActivity(){
    val binding: ActivityRegisterBinding by lazy{
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    // IWXAPI 是第三方 app 和微信通信的 openApi 接口
    private lateinit var api: IWXAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        setContentView(binding.root)
        regToWx()
        binding.whole.setOnClickListener {
            getWechatToken()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val unionID = intent?.getStringExtra(WX_UNION_ID)
        unionID?.isNotEmpty().let {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra(WX_UNION_ID, unionID)
            startActivity(intent)
            finish()
        }
    }


    private fun regToWx() {
        // 通过 WXAPIFactory 工厂，获取 IWXAPI 的实例
        api = WXAPIFactory.createWXAPI(this, WX_APP_ID, true)

        // 将应用的 appId 注册到微信
        api.registerApp(WX_APP_ID)

        //建议动态监听微信启动广播进行注册到微信
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                // 将该 app 注册到微信
                api.registerApp(WX_APP_ID)
            }
        }, IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP))
    }

    private fun getWechatToken() {
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo" //,snsapi_friend,snsapi_message,snsapi_contact
        req.state = "none"
        api.sendReq(req)
    }
}