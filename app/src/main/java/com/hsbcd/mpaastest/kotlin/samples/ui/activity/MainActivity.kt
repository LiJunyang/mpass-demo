package com.hsbcd.mpaastest.kotlin.samples.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.com.hsbc.hsbcchina.cert.R
import com.alipay.android.phone.scancode.export.ScanRequest
import com.alipay.android.phone.scancode.export.adapter.MPScan
import com.alipay.android.phone.scancode.export.adapter.MPScanCallbackAdapter
import com.alipay.android.phone.scancode.export.adapter.MPScanResult
import com.alipay.android.phone.scancode.export.adapter.MPScanStarter
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.login.LoginActivity
import com.mpaas.nebula.adapter.api.MPNebula
import com.ut.device.UTDevice


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mpaas)
        findViewById<Button>(R.id.btnScan).setOnClickListener {
            scan()
        }
        findViewById<Button>(R.id.btnOffline).setOnClickListener {
            offline()
        }
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            login()
        }
    }

    fun scan() {
        //获取设备id
        val utdid = UTDevice.getUtdid(this)
        Toast.makeText(this, "设备id:$utdid", Toast.LENGTH_SHORT).show();
        val scanRequest = ScanRequest()
        MPScan.startMPaasScanFullScreenActivity(
            this,
            scanRequest,
            object : MPScanCallbackAdapter() {
                override fun onScanFinish(
                    context: Context,
                    mpScanResult: MPScanResult?,
                    mpScanStarter: MPScanStarter?
                ): Boolean {
                    Toast.makeText(
                        applicationContext,
                        if (mpScanResult != null) mpScanResult.text else "没有识别到码",
                        Toast.LENGTH_SHORT
                    ).show()
                    (context as Activity).finish()
                    // 返回 true 表示该回调已消费，不需要再次回调
                    return true
                }
            })
    }

    fun offline() {
        //启动h5容器
        MPNebula.startUrl("https://www.aliyun.com")
    }

    private fun login() {

        val intent = Intent(this, LoginActivity::class.java)
        this.startActivity(intent)
    }
}
