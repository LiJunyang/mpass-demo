/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.login;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.hsbcd.mpaastest.kotlin.samples.model.ConnectionResult;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

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
        binding.loginButton.setEnabled(false);
        binding.loginButton.setOnClickListener(v -> onClickLogin());

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
                binding.loginButton.setEnabled(charSequence.length() > 0);
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

    private void bindLoginViewModel() {
        loginViewModel.getLoginResultData().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "登录成功", 3000);
            } else {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
            }

            //Intent intent = new Intent(this, HomeActivity.class);
            //this.startActivity(intent);
        });

        // 注：使用AlwaysActiveObserver，避免当前activity处于非激活状态时无法收到变更通知
        loginViewModel.getConnectionResultData().observeForever(result -> {
            if (result.getEvent() != ConnectionResult.Event.CONNECT_OK) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
            }

            // 被踢下线时，回退到登录页
            if (result.getEvent() == ConnectionResult.Event.KICKED) {
                Intent intent = new Intent(this, LoginActivity.class);
                this.startActivity(intent);
            }
        });
    }

}