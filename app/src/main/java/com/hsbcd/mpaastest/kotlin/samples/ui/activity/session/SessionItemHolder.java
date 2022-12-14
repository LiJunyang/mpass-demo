/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session;

import android.content.Context;
import android.text.Html;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageDeleteStatusEnum;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageRecallStatusEnum;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageSendStatusEnum;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageSpecialOpEnum;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.MessageSpecialOpVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.home.HomeActivity;
import com.hsbcd.mpaastest.kotlin.samples.util.DateUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.HighlightTextUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.MessageUtil;
import com.guanaj.easyswipemenulibrary.EasySwipeMenuLayout;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import cn.hsbcsd.mpaastest.databinding.FragmentSessionItemBinding;

/**
 * 会话列表项holder
 *
 * @author liyalong
 * @version MessageFragment.java, v 0.1 2022年07月29日 15:53 liyalong
 */
public class SessionItemHolder extends AbstractSessionItemHolder {

    private FragmentSessionItemBinding binding;

    private SessionViewModel sessionViewModel;

    public SessionItemHolder(Context context, View itemView) {
        super(context, itemView);

        binding = FragmentSessionItemBinding.bind(itemView);

        // 绑定会话数据
        sessionViewModel = new ViewModelProvider((AppCompatActivity) context).get(SessionViewModel.class);
    }

    /**
     * 绑定数据
     *
     * @param conversation
     */
    @Override
    public void bind(Conversation conversation) {
        this.c = conversation;

        // 绑定组件动作
        bindAction();

        // 会话头像
        renderSessionLogoUrl(binding.avatar);

        // 会话标题
        renderTitle(binding.title);

        // 会话置顶标记
        renderTopMode();

        // 免打扰标记
        renderNoDisturb();

        // 消息未读数
        renderUnReadCount();

        // 最近一条消息
        renderLastMessage();

        // 最近一条消息的时间戳
        renderLastMessageTime();

        // 特殊标记(特别关注/有人@我等)
        renderSpecialTag();
    }

    private void bindAction() {
        // 点击列表项时，查询当前会话，等待返回结果后，跳转到聊天消息页(在SessionFragment处理)
        binding.content.setOnClickListener(v -> sessionViewModel.querySingleConversation(c.getCid()));

        // 绑定侧滑菜单动作
        bindSwipeMenu();
    }

    private void bindSwipeMenu() {
        EasySwipeMenuLayout swipeMenu = binding.getRoot();

        // 如果宿主activity不是首页(例如搜索结果页)，则禁用侧滑菜单
        if (!(context instanceof HomeActivity)) {
            swipeMenu.setCanLeftSwipe(false);
            return;
        }

        // 点击侧滑菜单项-收藏
        binding.fav.setOnClickListener(v -> {
            goToAddFavoriteActivity();
            swipeMenu.resetStatus();
        });

        // 点击侧滑菜单项-置顶/取消置顶
        binding.setTop.setOnClickListener(v -> {
            swipeMenu.resetStatus();
        });

        // 点击侧滑菜单项-移除
        binding.remove.setOnClickListener(v -> {
            //ToastUtil.makeToast((AppCompatActivity) context, "敬请期待", 1000);
            // 该菜单项临时改为一键已读功能
            doRenderUnReadCount(0);
            swipeMenu.resetStatus();
        });
    }

    private void goToAddFavoriteActivity() {

    }

    private void renderTopMode() {
        if (c.isTopMode()) {
            binding.setTop.setText("Cancel Top");
            binding.setTopIcon.setVisibility(View.VISIBLE);
        } else {
            binding.setTop.setText("Top");
            binding.setTopIcon.setVisibility(View.GONE);
        }
    }

    private void renderNoDisturb() {
        binding.noDisturbIcon.setVisibility(c.isShieldMode() ? View.VISIBLE : View.GONE);
    }

    private void renderUnReadCount() {
        doRenderUnReadCount(c.getUnReadCount());
    }

    private void doRenderUnReadCount(int unReadCount) {
        if (unReadCount > 0) {
            binding.unReadMsgCount.setText(c.getUnReadCount() > 99 ? "99+" : String.valueOf(c.getUnReadCount()));
            binding.unReadMsgCount.setVisibility(View.VISIBLE);
        } else {
            binding.unReadMsgCount.setText(String.valueOf(0));
            binding.unReadMsgCount.setVisibility(View.GONE);
        }
    }

    private void renderLastMessage() {
        if (StringUtils.isBlank(c.getLastMsgContentType()) || c.getLastMsg() == null) {
            binding.lastTextMsg.setText("[No Msg]");
            return;
        }

        doRenderLastMessage(c.getLastMsg());
    }

    private void doRenderLastMessage(Message message) {
        if (message.getRecallStatus() != MessageRecallStatusEnum.NORMAL) {
            binding.lastTextMsg.setText("[已撤回]");
            return;
        }

        if (message.getDeleteStatus() == MessageDeleteStatusEnum.DELETE) {
            binding.lastTextMsg.setText("[已删除]");
            return;
        }

        if (message.getSendStatus() == MessageSendStatusEnum.CANCEL) {
            binding.lastTextMsg.setText("[已取消]");
            return;
        }

        if (message.getSendStatus() == MessageSendStatusEnum.SENDING) {
            binding.lastTextMsg.setText("[发送中]");
            return;
        }

//        if (message.getSendStatus() == MessageSendStatusEnum.NOT_SEND) {
//            binding.lastTextMsg.setText("[未发送]");
//            return;
//        }

        String contentText = MessageUtil.getContentText(message);
        binding.lastTextMsg.setText(Html.fromHtml(HighlightTextUtil.convertText(contentText)));
    }

    private void renderLastMessageTime() {
        if (c.getLastMsg() == null) {
            binding.msgSendTime.setText(StringUtils.EMPTY);
            return;
        }

        doRenderLastMessageTime(c.getLastMsg());
    }

    private void doRenderLastMessageTime(Message message) {
        long timestamp = message.getTimestamp();
        binding.msgSendTime.setText(DateUtil.getMessageSendTime(timestamp));
    }

    public void updateLastMessage(Message message) {
        doRenderLastMessage(message);

        int unReadCount = c.getUnReadCount() + 1;
        c.setUnReadCount(unReadCount);
        doRenderUnReadCount(unReadCount);

        doRenderLastMessageTime(message);

        // 更新并渲染会话对象的特殊标记
        updateSpecialTag(message);
        renderSpecialTag();
    }

    private void updateSpecialTag(Message<?> message) {
        if (CollectionUtils.isNotEmpty(message.getAtUsers())) {
            // 该消息是@我的
            boolean isAtMe = message.getAtUsers().stream().anyMatch(u -> StringUtils.equalsIgnoreCase(u.getUserId(),
                    AlipayCcmIMClient.getInstance().getCurrentUserId()));
            if (isAtMe) {
                c.setSpecialOpVOS(Arrays.asList(buildMessageSpecialOpVO(MessageSpecialOpEnum.AT_ME)));
            }

            // 该消息是@所有人的
            boolean isAtAll = message.getAtUsers().stream().anyMatch(
                    u -> StringUtils.equalsIgnoreCase(u.getUserId(), CommonConstants.AT_ALL_USER_ID));
            if (isAtAll) {
                c.setSpecialOpVOS(Arrays.asList(buildMessageSpecialOpVO(MessageSpecialOpEnum.AT_ALL)));
            }
        }
    }

    private MessageSpecialOpVO buildMessageSpecialOpVO(MessageSpecialOpEnum specialOpEnum) {
        MessageSpecialOpVO messageSpecialOpVO = new MessageSpecialOpVO();
        messageSpecialOpVO.setOpType(specialOpEnum.getCode());
        return messageSpecialOpVO;
    }

    private void renderSpecialTag() {
        if (!c.hasSpecialMessageMarks()) {
            binding.specialTag.setVisibility(View.GONE);
            return;
        }

        binding.specialTag.setVisibility(View.VISIBLE);

        if (c.hasSpecialConcern()) {
            binding.specialTag.setText("[特别关注]");
        }

        if (c.hasAtAllMessage()) {
            binding.specialTag.setText("[@所有人]");
        }

        if (c.hasAtMeMessage()) {
            binding.specialTag.setText("[有人@我]");
        }
    }

}