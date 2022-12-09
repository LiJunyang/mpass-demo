/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import cn.com.hsbc.hsbcchina.cert.R;
import com.alipay.fc.ccmimplus.common.message.domain.MessageContentType;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageRiskTypeEnum;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageSendStatusEnum;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.MessageReadUnReadStatusVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import cn.com.hsbc.hsbcchina.cert.databinding.MessageOfMeBinding;
import com.hsbcd.mpaastest.kotlin.samples.model.MediaSendingProgress;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.client.ImProgressCallback;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.service.MessageService;
import com.alipay.fc.ccmimplus.sdk.core.task.MultipartUploadCallback;
import com.alipay.fc.ccmimplus.sdk.core.task.MultipartUploadTask;
import com.alipay.fc.ccmimplus.sdk.core.task.MultipartUploadTaskManager;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.ChatActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.MessageReadStatusDialog;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts.UserDetailActivity;

import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;

/**
 * 自己发送消息holder
 *
 * @author liyalong
 * @version SendMessageHolder.java, v 0.1 2022年08月12日 11:54 liyalong
 */
public class SendMessageHolder extends AbstractMessageHolder {

    private MessageOfMeBinding binding;

    /**
     * 构造器
     *
     * @param context
     * @param itemView
     */
    public SendMessageHolder(Context context, View itemView, boolean topicMessageMode) {
        super(context, itemView);

        binding = MessageOfMeBinding.bind(itemView);
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

        // 仅文本消息支持撤回后重新编辑
        if (message.getContentTypeCode() == MessageContentType.TEXT_VALUE ||
                message.getContentTypeCode() == MessageContentType.FACE_VALUE) {
            binding.reEditRecalled.setVisibility(View.VISIBLE);
            binding.reEditRecalled.setOnClickListener(v -> {
                ((ChatActivity) context).reEditRecalledMessage(message);
            });
        } else {
            binding.reEditRecalled.setVisibility(View.GONE);
        }
    }

    /**
     * 绑定消息
     *
     * @param message
     */
    @Override
    public void bind(Message message) {
        // 暂不需要展示自己的头像
//        UserInfoVO userInfoVO = UserCacheManager.getInstance().getCurrentUserInfo();
//        String avatarUrl = (userInfoVO == null) ? CommonConstants.DEFAULT_AVATAR : userInfoVO.getAvatarUrl();
//        Glide.with(context).load(avatarUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.myAvatar);

        // 绑定组件动作
        bindAction();

        if (topicMessageMode) {
            binding.reSendButton.setVisibility(View.GONE);
            binding.readStatus.setVisibility(View.GONE);
            binding.quoteReplyCount.setVisibility(View.GONE);
        } else {
            Message localMessage = getLocalMessage(message);
            
            if (localMessage != null) {
                // 渲染消息发送状态
                renderSendStatus(localMessage);

                // 渲染消息已读状态
                renderReadStatus(localMessage);
            }

            // 渲染引用回复计数
            renderQuoteReplyCount(binding.quoteReplyCount, message);
        }

        // 绑定消息弹出菜单
        bindMessagePopupMenu(message);

        // 渲染消息内容
        renderMessageContent(message);
    }

    private Message getLocalMessage(Message message) {
        // 先用localId查
        Message localMessage = MessageService.getInstance().queryOneLocalMessage(message.getTntInstId(),
                message.getSid(), message.getLocalId());

        // 再用messageId查
        if (localMessage == null) {
            localMessage = MessageService.getInstance().queryOneMessageByMid(message.getTntInstId(),
                    message.getSid(), NumberUtils.toLong(message.getId()));
        }

        return localMessage;
    }

    private void bindAction() {
        binding.myAvatar.setOnClickListener(v -> {
            String currentUserId = AlipayCcmIMClient.getInstance().getCurrentUserId();

            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra(CommonConstants.USER_ID, currentUserId);
            context.startActivity(intent);
        });
    }

    private void bindReSendEvent(Message message) {
        binding.reSendButton.setOnClickListener(v -> ((ChatActivity) this.context).reSendMessage(message));
    }

    /**
     * 隐藏消息重发按钮
     */
    public void hideReSendButton() {
        binding.reSendButton.setVisibility(View.GONE);
    }

    private void renderSendStatus(Message message) {
        // 消息状态为发送失败/超时，或消息内容质检结果为拒绝
        if (message.getSendStatus() == MessageSendStatusEnum.SEND_BUT_FAILED ||
                message.getSendStatus() == MessageSendStatusEnum.SEND_BUT_TIMEOUT ||
                message.getSendStatus() == MessageSendStatusEnum.NOT_SEND ||
                message.getRiskType() == MessageRiskTypeEnum.REJECTED) {
            binding.reSendButton.setVisibility(View.VISIBLE);

            // 绑定重新发送逻辑
            bindReSendEvent(message);
        }
        // 消息状态已发送
        else if (message.getSendStatus() == MessageSendStatusEnum.ALREADY_SEND) {
            // 隐藏重发按钮
            binding.reSendButton.setVisibility(View.GONE);

            // 清除发送进度
            finishSendingProgress(MessageContentType.forNumber(message.getContentTypeCode()));
        }
        // 消息状态为发送中
        else if (message.getSendStatus() == MessageSendStatusEnum.SENDING) {
            binding.reSendButton.setVisibility(View.GONE);

            MultipartUploadTask task = MultipartUploadTaskManager.getInstance().getTask(message.getLocalId());
            if (task != null) {
                MultipartUploadTask.ProgressInfo progressInfo = task.getProgressInfo();

                // 渲染发送进度
                task.setUploadCallback(new MultipartUploadCallback() {
                    @Override
                    public void beforeUpload(File file, String fileName) {
                    }

                    @Override
                    public void onProgress(long total, long done, boolean finish) {
                        progressInfo.transferToPercent(new ImProgressCallback() {
                            @Override
                            public void onProgress(long total, long sent, boolean done) {
                                MediaSendingProgress progress = new MediaSendingProgress(total, sent, done,
                                        MessageContentType.forNumber(message.getContentTypeCode()));
                                updateSendingProgress(progress);
                            }
                        });
                    }

                    @Override
                    public void afterUpload(File file, String fileName, Object contentMeta) {
                    }
                });
            }
        }
    }

    private void renderReadStatus(Message message) {
        // 如果消息未发送成功，则不需要渲染已读/未读状态
        if (message.getSendStatus() != MessageSendStatusEnum.ALREADY_SEND) {
            binding.readStatus.setVisibility(View.GONE);
            return;
        }

        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        MessageReadUnReadStatusVO readStatusVO = message.getReadStatusVO();

        if (readStatusVO != null) {
            if (readStatusVO.isAllRead()) {
                renderRead(c.isSingle());
            } else {
                renderUnRead(c.isSingle(), readStatusVO.getUnReadUserCount(), message);
            }
        } else {
            renderUnRead(c.isSingle(), (c.getGroup() == null) ? 0 : c.getGroup().getMemberCount() - 1, message);
        }
    }

    private void renderRead(boolean single) {
        binding.readStatus.setVisibility(View.VISIBLE);
        binding.readStatus.setTextColor(context.getResources().getColor(R.color.middle_gray));
        binding.readStatus.setText(single ? "已读" : "全部已读");
    }

    private void renderUnRead(boolean single, int unReadUserCount, Message message) {
        binding.readStatus.setVisibility(View.VISIBLE);
        binding.readStatus.setTextColor(context.getResources().getColor(R.color.ant_blue));
        binding.readStatus.setText(single ? "unread" : String.format("%d ppl unread", unReadUserCount));

        if (!single) {
            binding.readStatus.setOnClickListener(v -> {
                MessageReadStatusDialog dialog = new MessageReadStatusDialog(context, message);
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "");
            });
        }
    }

}
