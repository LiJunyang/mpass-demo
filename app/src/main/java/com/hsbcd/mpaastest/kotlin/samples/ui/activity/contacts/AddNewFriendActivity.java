/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import cn.hsbcsd.mpaastest.databinding.ActivityAddNewFriendBinding;

/**
 * 添加好友页
 *
 * @author liyalong
 * @version AddNewFriendActivity.java, v 0.1 2022年08月09日 11:13 liyalong
 */
public class AddNewFriendActivity extends AppCompatActivity {

    private ActivityAddNewFriendBinding binding;

    private ContactsViewModel contactsViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddNewFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.goBack.setOnClickListener(v -> onClickGoBack());

        binding.addFriend.setEnabled(false);
        binding.addFriend.setOnClickListener(v -> onClickAddFriend());

        binding.inputUserId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addFriend.setEnabled(charSequence.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        contactsViewModel = new ViewModelProvider(this).get(ContactsViewModel.class);

        contactsViewModel.getAddFriendResult().observe(this, result -> {
            ToastUtil.makeToast(this, result.getMessage(), 2000);
            super.onBackPressed();
        });
    }

    public void onClickGoBack() {
        super.onBackPressed();
    }

    public void onClickAddFriend() {
        String userId = binding.inputUserId.getText().toString();
        contactsViewModel.addFriend(userId);
    }

}