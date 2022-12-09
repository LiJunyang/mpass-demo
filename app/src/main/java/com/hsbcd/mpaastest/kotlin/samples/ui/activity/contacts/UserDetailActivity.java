/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.ChatActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.SessionViewModel;
import com.hsbcd.mpaastest.kotlin.samples.util.CopyUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.apache.commons.lang3.StringUtils;

import cn.com.hsbc.hsbcchina.cert.databinding.ActivityUserDetailBinding;

/**
 * 用户详情
 *
 * @author liyalong
 * @version UserDetailActivity.java, v 0.1 2022年08月02日 11:02 liyalong
 */
public class UserDetailActivity extends AppCompatActivity {

    private static final String DEFAULT_VALUE = "unset";

    private ActivityUserDetailBinding binding;

    private String userId;

    //private ContactsViewModel contactsViewModel;

    private SessionViewModel sessionViewModel;

    //private FavoriteViewModel favoriteViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = getIntent().getStringExtra(CommonConstants.USER_ID);

        binding = ActivityUserDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindAction();

//        // 绑定联系人数据
//        contactsViewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
//        bindContactsViewModel();

        // 绑定会话数据
        sessionViewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        bindSessionViewModel();

//        // 绑定收藏数据
//        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
//        bindFavoriteViewModel();
    }

    @Override
    public void onStart() {
        super.onStart();

        // 加载用户信息
        loadUserInfo();
    }

    private void loadUserInfo() {
//        // 查询指定用户详情
//        contactsViewModel.queryUserInfo(userId);

//        // 查询对该用户的收藏标签
//        favoriteViewModel.queryTargetFavoriteTags(MarkTargetEnum.USER, userId);
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.onBackPressed());
        binding.userId.setOnClickListener(v -> onClickUserId());
        binding.userTagName.setOnClickListener(v -> onClickUserTag());
        binding.sendMessageButton.setOnClickListener(v -> onClickSendMessage());
    }

    private void onClickUserId() {
        CopyUtil.copyToClipboard(this, userId);
        ToastUtil.makeToast(this, "用户ID已复制到剪切版", 1000);
    }

    private void onClickUserTag() {
        // 打开新增收藏页
//        Intent intent = new Intent(this, AddFavoriteContentActivity.class);
//        intent.putExtra(AddFavoriteContentActivity.TARGET_TYPE, CollectType.USER.getCode());
//        intent.putExtra(AddFavoriteContentActivity.TARGET_VALUE, userId);
//        this.startActivity(intent);
    }

    private void onClickSendMessage() {
        // 点击发消息时，创建单聊会话，等待创建完成后再打开消息页
        String userId = getIntent().getStringExtra(CommonConstants.USER_ID);
        sessionViewModel.createSingleConversation(userId);
    }

    private void bindContactsViewModel() {
//        // 用户详情查询结果通知
//        contactsViewModel.getUserInfoResult().observe(this, result -> {
//            if (!result.isSuccess()) {
//                ToastUtil.makeToast(this, result.getMessage(), 3000);
//                return;
//            }
//
//            // 渲染用户详情
//            renderUserInfo(result.getData());
//        });
    }

    private void bindSessionViewModel() {
        // 单聊会话查询结果通知
        sessionViewModel.getConversationResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            // 设置当前会话
            AlipayCcmIMClient.getInstance().getConversationManager().setCurrentConversation(result.getData());

            // 打开会话消息页
            Intent intent = new Intent(this, ChatActivity.class);
            this.startActivity(intent);
        });
    }

    private void bindFavoriteViewModel() {
//        // 联系人收藏标签名称列表查询结果通知
//        favoriteViewModel.getFavoriteTagNameListResult().observe(this, result -> {
//            if (!result.isSuccess()) {
//                //ToastUtil.makeToast(this, result.getMessage(), 3000);
//                return;
//            }
//
//            if (CollectionUtils.isEmpty(result.getData())) {
//                return;
//            }
//
//            binding.userTagName.setText(StringUtils.join(result.getData(), CommonConstants.COMMA));
//        });
    }

    private void renderUserInfo(UserInfoVO data) {
        Glide.with(this).load(
                StringUtils.defaultIfBlank(data.getAvatarUrl(), CommonConstants.DEFAULT_AVATAR)).diskCacheStrategy(
                DiskCacheStrategy.ALL).into(binding.userAvatar);

        String userName = StringUtils.defaultIfBlank(data.getNickName(), data.getUserName());
        binding.titleLabel.setText(userName);
        binding.userName.setText(userName);
        binding.userId.setText(String.format("ID: %s", data.getUserId()));
        binding.userMark.setText(StringUtils.defaultIfBlank(data.getMark(), "unset"));
    }
}