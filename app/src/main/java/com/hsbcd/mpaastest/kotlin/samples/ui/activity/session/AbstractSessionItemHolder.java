/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import cn.hsbcsd.mpaastest.R;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;
import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

/**
 * 会话列表项holder基类
 *
 * @author liyalong
 * @version AbstractSessionItemHolder.java, v 0.1 2022年08月25日 17:43 liyalong
 */
public abstract class AbstractSessionItemHolder extends BaseListItemHolder<Conversation> {

    protected Conversation c;

    public AbstractSessionItemHolder(Context context, View itemView) {
        super(context, itemView);
    }

    public AbstractSessionItemHolder(Context context, View itemView, OnItemActionListener onItemActionListener) {
        super(context, itemView, onItemActionListener);
    }

    protected void renderSessionLogoUrl(ImageView avatarView) {
        // 单聊会话
        if (c.isSingle()) {
            String avatarUrl = c.getSessionLogoUrl();
            Glide.with(avatarView.getContext()).load(
                    StringUtils.defaultIfBlank(avatarUrl, CommonConstants.DEFAULT_AVATAR)).into(avatarView);
        }
        // 群聊会话
        else if (c.isGroupConversation()) {
            Glide.with(avatarView.getContext()).load(
                    StringUtils.defaultIfBlank(c.getGroup().getLogoUrl(), CommonConstants.DEFAULT_GROUP_LOGO)).into(
                    avatarView);
        }
        // 通知会话
        else if (c.isNotify()) {
            Glide.with(avatarView.getContext()).load(
                    ((AppCompatActivity) context).getDrawable(R.drawable.ic_notify)).into(
                    avatarView);
        }
    }

    protected void renderTitle(TextView titleView) {
        if (StringUtils.isNotBlank(c.getSessionName())) {
            titleView.setText(c.getSessionName());
            return;
        }

        if (c.isSingle() || c.isGroupConversation()) {
            titleView.setText(c.getConversationName());
        } else if (c.isNotify()) {
            titleView.setText("工作通知");
        }
    }

}