/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.alipay.fc.ccmimplus.sdk.core.client.ImCallback;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts.UserDetailActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.UserCacheManager;
import com.hsbcd.mpaastest.kotlin.samples.util.DateUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.apache.commons.lang3.StringUtils;

/**
 * 指定消息列表项
 *
 * @author liyalong
 * @version SpecifiedMessageHolder.java, v 0.1 2022年08月26日 21:54 liyalong
 */
public abstract class SpecifiedMessageHolder extends AbstractMessageHolder {

    private ImageView senderAvatar;

    private TextView senderUserName;

    private TextView messageSendTime;

    /**
     * 构造器
     *
     * @param context
     * @param itemView
     */
    public SpecifiedMessageHolder(Context context, View itemView) {
        super(context, itemView);
    }

    public SpecifiedMessageHolder(Context context, View itemView, OnItemActionListener onItemActionListener) {
        super(context, itemView, onItemActionListener);
    }

    protected void initBindingView(ImageView senderAvatar, TextView senderUserName, TextView messageSendTime) {
        this.senderAvatar = senderAvatar;
        this.senderUserName = senderUserName;
        this.messageSendTime = messageSendTime;
    }

    @Override
    protected void showNormalMessage() {
    }

    @Override
    protected void renderRecalledMessage(Message message) {
    }

    /**
     * 绑定消息
     *
     * @param message
     */
    @Override
    public void bind(Message message) {
        // 绑定组件动作
        bindAction(message);

        // 渲染消息发送人的用户信息
        renderSenderUserInfo(message);

        // 渲染消息内容
        renderMessageContent(message);

        // 渲染消息发送时间
        renderMessageSendTime(message);
    }

    private void bindAction(Message message) {
        senderAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra(CommonConstants.USER_ID, message.getFrom().getUid());
            context.startActivity(intent);
        });
    }

    private void renderSenderUserInfo(Message message) {
        String userId = message.getFrom().getUid();

        UserCacheManager.getInstance().queryUser(userId, false, new ImCallback<UserInfoVO>() {
            @Override
            public void onSuccess(UserInfoVO data) {
                handler.post(() -> {
                    String userNick = StringUtils.defaultIfBlank(data.getNickName(), data.getUserName());
                    doRenderUserInfo(userNick, data.getAvatarUrl());
                });
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                handler.post(() -> {
                    doRenderUserInfo(userId, CommonConstants.DEFAULT_AVATAR);
                });
            }
        });
    }

    private void doRenderUserInfo(String userName, String avatarUrl) {
        senderUserName.setText(userName);
        Glide.with(context).load(
                StringUtils.defaultIfBlank(avatarUrl, CommonConstants.DEFAULT_AVATAR)).diskCacheStrategy(
                DiskCacheStrategy.ALL).into(senderAvatar);
    }

    private void renderMessageSendTime(Message message) {
        long timestamp = message.getTimestamp();
        messageSendTime.setText(DateUtil.getMessageSendTime(timestamp));
    }

}