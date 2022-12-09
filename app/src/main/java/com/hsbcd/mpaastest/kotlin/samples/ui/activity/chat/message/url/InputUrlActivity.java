/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.url;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import cn.com.hsbc.hsbcchina.cert.databinding.ActivityInputUrlBinding;
import com.alipay.fc.ccmimplus.sdk.core.util.UrlUtils;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

/**
 * 输入链接页
 *
 * @author liyalong
 * @version InputUrlActivity.java, v 0.1 2022年08月23日 10:13 liyalong
 */
public class InputUrlActivity extends AppCompatActivity {

    private ActivityInputUrlBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInputUrlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.goBack.setOnClickListener(v -> onClickGoBack());

        binding.sendUrl.setEnabled(false);
        binding.sendUrl.setOnClickListener(v -> onClickSendUrl());

        binding.inputUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.sendUrl.setEnabled(charSequence.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public void onClickGoBack() {
        super.onBackPressed();
    }

    public void onClickSendUrl() {
        String url = binding.inputUrl.getText().toString();

        if (!UrlUtils.isPureUrl(url)) {
            ToastUtil.makeToast(this, "非法的链接地址，请重新输入", 1000);
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("url", url);

        setResult(RESULT_OK, intent);
        finish();
    }

}