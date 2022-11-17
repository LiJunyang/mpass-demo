/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import cn.hsbcsd.mpaastest.R;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import cn.hsbcsd.mpaastest.databinding.MessageOfOtherBinding;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.client.ImCallback;
import com.alipay.fc.ccmimplus.sdk.core.conversation.ConversationManager;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.GroupRelation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.ChatActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts.UserDetailActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.UserCacheManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 收到消息holder
 *
 * @author liyalong
 * @version ReceivedMessageHolder.java, v 0.1 2022年08月12日 11:54 liyalong
 */
public class ReceivedMessageHolder extends AbstractMessageHolder {

    private MessageOfOtherBinding binding;

    /**
     * 构造器
     *
     * @param context
     * @param itemView
     */
    public ReceivedMessageHolder(Context context, View itemView, boolean topicMessageMode) {
        super(context, itemView);

        binding = MessageOfOtherBinding.bind(itemView);
        initMessageBinding(binding.messageContainer, binding.selectMessageCheckBox);

        setTopicMessageMode(topicMessageMode);
    }

    @Override
    protected void showNormalMessage() {
        binding.normalMessageLayout.setVisibility(View.VISIBLE);
        binding.recalledMessageLayout.setVisibility(View.GONE);
    }

    @Override
    protected void renderRecalledMessage(Message message) {
        binding.normalMessageLayout.setVisibility(View.GONE);
        binding.recalledMessageLayout.setVisibility(View.VISIBLE);

        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        if (c.isSingle()) {
            binding.recalledMessageDesc.setText("对方撤回了一条消息");
        } else {
            renderGroupMemberInfo(message, (userName, avatarUrl) -> {
                binding.recalledMessageDesc.setText(String.format("%s 撤回了一条消息", userName));
            });
        }
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

        if (topicMessageMode) {
            binding.quoteReplyCount.setVisibility(View.GONE);
        } else {
            // 渲染引用回复计数
            renderQuoteReplyCount(binding.quoteReplyCount, message);
        }

        // 绑定消息弹出菜单
        bindMessagePopupMenu(message);

        // 渲染消息发送人的用户信息
        renderSenderUserInfo(message);

        // 渲染消息内容
        renderMessageContent(message);
    }

    private void bindAction(Message message) {
        String senderUserId = message.getFrom().getUid();

        // 短按发送人头像，打开用户详情页
        binding.senderAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra(CommonConstants.USER_ID, senderUserId);
            context.startActivity(intent);
        });
    }

    private void bindLongClickSenderAvatarAction(String senderUserId, String senderUserName) {
        // 长按发送人头像，@该用户，仅群聊会话有效
        if (context instanceof ChatActivity) {
            binding.senderAvatar.setOnLongClickListener(v -> {
                ((ChatActivity) context).appendAtUser(senderUserId, senderUserName);

                // 响应长按事件后，不再响应短按事件
                return true;
            });
        }
    }

    private void renderSenderUserInfo(Message message) {
        Conversation c = ConversationManager.getInstance().getCurrentConversation();

        // 密聊会话隐藏发送人用户信息
        if (c.isSecretChat()) {
            binding.senderUserName.setText(StringUtils.EMPTY);
            binding.senderAvatar.setBackground(context.getDrawable(R.drawable.ic_secret_chat_avatar));
            return;
        }

        if (c.isGroupConversation()) {
            renderGroupMemberInfo(message, (userName, avatarUrl) -> {
                doRenderSenderUserInfo(userName, avatarUrl);
                bindLongClickSenderAvatarAction(message.getFrom().getUid(), userName);
            });
        } else {
            renderUserInfo(message);
        }
    }

    private void renderGroupMemberInfo(Message message, GroupMemberCallback callback) {
        String cid = message.getSid();
        String userId = message.getFrom().getUid();

        UserCacheManager.getInstance().batchQueryGroupMembers(cid, Arrays.asList(userId),
                new ImCallback<List<GroupRelation>>() {
                    @Override
                    public void onSuccess(List<GroupRelation> data) {
                        // 如果查询群成员信息失败，则兜底查询用户信息，兼容用户已退群的情况，下同
                        if (CollectionUtils.isEmpty(data)) {
                            renderUserInfo(message);
                            return;
                        }

                        handler.post(() -> {
                            GroupRelation member = data.get(0);
                            String userNick = StringUtils.defaultIfBlank(member.getUserNick(),
                                    StringUtils.defaultIfBlank(member.getNickName(), member.getUserName()));
                            callback.onCallback(userNick, member.getAvatarUrl());
                        });
                    }

                    @Override
                    public void onError(String errorCode, String m, Throwable t) {
                        renderUserInfo(message);
                    }
                });
    }

    private void renderUserInfo(Message message) {
        String userId = message.getFrom().getUid();

        UserCacheManager.getInstance().queryUser(userId, false, new ImCallback<UserInfoVO>() {
            @Override
            public void onSuccess(UserInfoVO data) {
                handler.post(() -> {
                    String userNick = StringUtils.defaultIfBlank(data.getNickName(), data.getUserName());
                    doRenderSenderUserInfo(userNick, data.getAvatarUrl());
                });
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                handler.post(() -> {
                    doRenderSenderUserInfo(userId, CommonConstants.DEFAULT_AVATAR);
                });
            }
        });
    }

    private void doRenderSenderUserInfo(String userName, String avatarUrl) {
        binding.senderUserName.setText(userName);
        Glide.with(context).load(
                StringUtils.defaultIfBlank(avatarUrl, CommonConstants.DEFAULT_AVATAR)).diskCacheStrategy(
                DiskCacheStrategy.ALL).into(binding.senderAvatar);
    }

    private interface GroupMemberCallback {
        void onCallback(String userName, String avatarUrl);
    }
}