/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import cn.hsbcsd.mpaastest.R;
import cn.hsbcsd.mpaastest.databinding.ActivityUpdateTextSettingBinding;
import com.hsbcd.mpaastest.kotlin.samples.enums.UpdateTextSettingTypeEnum;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.me.MineViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.UserCacheManager;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

/**
 * 更新文本类设置项(例如群名称/群公告等)的通用页面
 *
 * @author liyalong
 * @version UpdateTextSettingActivity.java, v 0.1 2022年08月10日 10:13 liyalong
 */
public class UpdateTextSettingActivity extends AppCompatActivity {

    public static final String TYPE_KEY = "type";

    public static final String READ_ONLY_KEY = "readOnly";

    private ActivityUpdateTextSettingBinding binding;

    private MineViewModel mineViewModel;

    private GroupChatSettingViewModel groupChatSettingViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUpdateTextSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.goBack.setOnClickListener(v -> super.onBackPressed());
        binding.saveButton.setEnabled(false);

        boolean readOnly = getIntent().getBooleanExtra(READ_ONLY_KEY, false);
        if (readOnly) {
            bindReadOnly();
        } else {
            bindInputText();
        }

        // 根据待更新设置类型，绑定不同的动作
        bindActionByUpdateType();

        // 绑定个人信息数据
        mineViewModel = new ViewModelProvider(this).get(MineViewModel.class);
        bindMineViewModel();

        // 绑定群聊设置数据
        groupChatSettingViewModel = new ViewModelProvider(this).get(GroupChatSettingViewModel.class);
        bindGroupChatSettingViewModel();
    }

    private void bindInputText() {
        binding.inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.saveButton.setEnabled(charSequence.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void bindReadOnly() {
        // 输入框设置为灰显且不可操作，实现只读效果
        binding.inputText.setKeyListener(null);
        binding.inputText.setBackgroundColor(getResources().getColor(R.color.light_gray2));

        binding.readOnlyLabel.setVisibility(View.VISIBLE);
    }

    private void bindActionByUpdateType() {
        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        UpdateTextSettingTypeEnum type = UpdateTextSettingTypeEnum.valueOf(getIntent().getStringExtra(TYPE_KEY));

        switch (type) {
            case USER_NAME: {
                binding.titleLabel.setText("修改名称");
                binding.inputText.setText(UserCacheManager.getInstance().getCurrentUserInfo().getUserName());

                binding.saveButton.setOnClickListener(v -> {
                    String text = binding.inputText.getText().toString();
                    if (text.length() > 64) {
                        ToastUtil.makeToast(this, "用户名称不能超过64个字符", 1000);
                        return;
                    }

                    mineViewModel.updateUserName(text);
                });
                break;
            }
            case USER_MARK: {
                binding.titleLabel.setText("修改个性签名");
                binding.inputText.setText(UserCacheManager.getInstance().getCurrentUserInfo().getMark());

                binding.saveButton.setOnClickListener(v -> {
                    String text = binding.inputText.getText().toString();
                    if (text.length() > 128) {
                        ToastUtil.makeToast(this, "个性签名不能超过128个字符", 1000);
                        return;
                    }

                    mineViewModel.updateUserMark(text);
                });
                break;
            }
            case GROUP_NICK_NAME: {
                binding.titleLabel.setText("修改我在本群的昵称");
                binding.inputText.setText(c.getGroup().getRelationOfMe().getUserNick());

                binding.saveButton.setOnClickListener(v -> {
                    String text = binding.inputText.getText().toString();
                    if (text.length() > 32) {
                        ToastUtil.makeToast(this, "群昵称不能超过32个字符", 1000);
                        return;
                    }

                    groupChatSettingViewModel.updateGroupNickName(c, text);
                });
                break;
            }
            case GROUP_MEMO: {
                binding.titleLabel.setText("修改我对本群的备注");
                binding.inputText.setText(c.getGroup().getRelationOfMe().getUserMark());

                binding.saveButton.setOnClickListener(v -> {
                    String text = binding.inputText.getText().toString();
                    if (text.length() > 256) {
                        ToastUtil.makeToast(this, "群备注不能超过256个字符", 1000);
                        return;
                    }

                    groupChatSettingViewModel.updateGroupMemo(c, text);
                });
                break;
            }
            case GROUP_NAME: {
                binding.titleLabel.setText("修改群名称");
                binding.inputText.setText(c.getGroup().getName());

                binding.saveButton.setOnClickListener(v -> {
                    String text = binding.inputText.getText().toString();
                    if (text.length() > 128) {
                        ToastUtil.makeToast(this, "群名称不能超过128个字符", 1000);
                        return;
                    }

                    groupChatSettingViewModel.updateGroupName(c, text);
                });
                break;
            }
            case GROUP_DESC: {
                binding.titleLabel.setText("修改群介绍");
                binding.inputText.setText(c.getGroup().getRemark());

                binding.saveButton.setOnClickListener(v -> {
                    String text = binding.inputText.getText().toString();
                    if (text.length() > 1024) {
                        ToastUtil.makeToast(this, "群介绍不能超过1024个字符", 1000);
                        return;
                    }

                    groupChatSettingViewModel.updateGroupDesc(c, text);
                });
                break;
            }
            case GROUP_NOTICE: {
                binding.titleLabel.setText("修改群公告");
                binding.inputText.setText(c.getGroup().getNotice());

                binding.saveButton.setOnClickListener(v -> {
                    String text = binding.inputText.getText().toString();
                    if (text.length() > 1024) {
                        ToastUtil.makeToast(this, "群公告不能超过1024个字符", 1000);
                        return;
                    }

                    groupChatSettingViewModel.updateGroupNotice(c, text);
                });
                break;
            }
            default:
                break;
        }
    }

    private void bindMineViewModel() {
        mineViewModel.getUpdateUserInfoResult().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "更新成功", 1000);
            } else {
                ToastUtil.makeToast(this, "更新失败: " + result.getMessage(), 3000);
            }

            super.onBackPressed();
        });
    }

    private void bindGroupChatSettingViewModel() {
        groupChatSettingViewModel.getUpdateSettingResult().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "更新成功", 1000);
            } else {
                ToastUtil.makeToast(this, "更新失败: " + result.getMessage(), 3000);
            }

            super.onBackPressed();
        });
    }
}