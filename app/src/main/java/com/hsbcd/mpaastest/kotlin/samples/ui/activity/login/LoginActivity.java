/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alipay.android.phone.scancode.export.ScanRequest;
import com.alipay.android.phone.scancode.export.adapter.MPScan;
import com.alipay.android.phone.scancode.export.adapter.MPScanCallbackAdapter;
import com.alipay.android.phone.scancode.export.adapter.MPScanResult;
import com.alipay.android.phone.scancode.export.adapter.MPScanStarter;
import com.hsbcd.mpaastest.kotlin.samples.model.ConnectionResult;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.home.HomeActivity;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;
import com.mpaas.nebula.adapter.api.MPNebula;
import com.ut.device.UTDevice;

import java.util.List;

import cn.hsbcsd.mpaastest.databinding.ActivityLoginBinding;


/**
 * 登录页
 *
 * @author liyalong
 * @version LoginActivity.java, v 0.1 2022年07月28日 19:05 liyalong
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindAction();

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        bindLoginViewModel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // TODO 移除长连接livedata的观察者，避免内存泄露
        // loginViewModel.getConnectionResultData().removeObserver();
    }

    private void bindAction() {

        binding.btnScan.setOnClickListener(v -> onClickScan());
        binding.btnOffline.setOnClickListener(v -> onClickOffline());

        binding.btnLogon.setEnabled(false);
        binding.btnLogon.setOnClickListener(v -> onClickLogin());

        binding.title.setOnClickListener(v -> {
            EnvSwitchDialog dialog = new EnvSwitchDialog(this);
            dialog.show(getSupportFragmentManager(), "");
        });

        binding.help2.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://ccmimplus.cloud.alipay.com/portal/index.htm#/implus"));
            this.startActivity(intent);
        });

        binding.inputUserId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.btnLogon.setEnabled(charSequence.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void onClickLogin() {
        String userId = binding.inputUserId.getText().toString();
        loginViewModel.login(LoginActivity.this, userId);
    }

    private void onClickOffline() {
        MPNebula.startUrl("https://www.aliyun.com");
    }

    private void onClickScan() {
        //获取设备id
        String utdid = UTDevice.getUtdid(this);
        ToastUtil.makeToast(this, "device id:" + utdid, 3000);
        ScanRequest scanRequest = new ScanRequest();
        MPScan.startMPaasScanFullScreenActivity(
                this,
                scanRequest,
                new MPScanCallbackAdapter() {

                    @Override
                    public boolean onScanFinish(Context context, MPScanResult mpScanResult, MPScanStarter mpScanStarter) {
                        ToastUtil.makeToast(((Activity)context), (mpScanResult != null)? mpScanResult.getText() : "没有识别到码", 3000);
                        ((Activity)context).finish();
                        // 返回 true 表示该回调已消费，不需要再次回调
                        return true;
                    }
                }
        );
    }

    private void bindLoginViewModel() {
        loginViewModel.getLoginResultData().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "Login Success", 3000);
            } else {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
            }

            Intent intent = new Intent(this, HomeActivity.class);
            this.startActivity(intent);
        });

        // 注：使用AlwaysActiveObserver，避免当前activity处于非激活状态时无法收到变更通知
        loginViewModel.getConnectionResultData().observeForever(result -> {
            if (result.getEvent() != ConnectionResult.Event.CONNECT_OK) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
            }

            // 被踢下线时，回退到登录页
            if (result.getEvent() == ConnectionResult.Event.KICKED) {
                ToastUtil.makeToast(this, "You has been kicked, pls login again", 3000);
                Intent intent = new Intent(this, LoginActivity.class);
                this.startActivity(intent);
            }
        });
    }

}