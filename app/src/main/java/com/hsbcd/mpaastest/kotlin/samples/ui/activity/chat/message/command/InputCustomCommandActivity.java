/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.command;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import cn.hsbcsd.mpaastest.databinding.ActivityInputCustomCommandBinding;

/**
 * 输入自定义命令页
 *
 * @author liyalong
 * @version InputCustomCommandActivity.java, v 0.1 2022年08月23日 17:53 liyalong
 */
public class InputCustomCommandActivity extends AppCompatActivity {

    private ActivityInputCustomCommandBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInputCustomCommandBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.goBack.setOnClickListener(v -> onClickGoBack());

        binding.sendCommand.setEnabled(false);
        binding.sendCommand.setOnClickListener(v -> onClickSendUrl());

        binding.inputCommand.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.sendCommand.setEnabled(charSequence.length() > 0);
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
        String command = binding.inputCommand.getText().toString();

        Intent intent = new Intent();
        intent.putExtra("command", command);

        setResult(RESULT_OK, intent);
        finish();
    }

}