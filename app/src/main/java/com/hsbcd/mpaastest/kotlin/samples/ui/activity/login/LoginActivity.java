/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import cn.hsbcsd.mpaastest.databinding.ActivityLoginBinding;


/**
 * 登录页
 *
 * @author liyalong
 * @version LoginActivity.java, v 0.1 2022年07月28日 19:05 liyalong
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindAction();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void bindAction() {
        binding.loginButton.setEnabled(false);
        binding.loginButton.setOnClickListener(v -> onClickLogin());

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
    }

}