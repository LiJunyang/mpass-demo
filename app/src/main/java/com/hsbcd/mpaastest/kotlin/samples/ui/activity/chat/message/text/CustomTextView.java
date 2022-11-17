/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.text;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import cn.hsbcsd.mpaastest.R;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageReadStatusEnum;
import com.alipay.fc.ccmimplus.common.service.facade.model.User;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.alipay.fc.ccmimplus.sdk.core.client.ImCallback;
import com.alipay.fc.ccmimplus.sdk.core.config.face.FaceIconEmoji;
import com.alipay.fc.ccmimplus.sdk.core.config.face.FaceIconManager;
import com.alipay.fc.ccmimplus.sdk.core.constants.Constants;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.GroupRelation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts.UserDetailActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.UserCacheManager;
import com.hsbcd.mpaastest.kotlin.samples.util.ImageUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.MessageUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.RegexUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 自定义文本视图，支持展示表情和@的人
 *
 * @author liyalong
 * @version ChatActivity.java, v 0.1 2022年10月14日 15:50 liyalong
 */
public class CustomTextView extends AppCompatTextView {

    /**
     * 匹配表情占位符的正则表达式
     * 示例："[奸笑]"
     */
    private static final Pattern EMOJI_PATTERN = Pattern.compile("\\[(.+?)\\]");

    /**
     * 匹配@用户的正则表达式
     * 示例："@aaa "，注意后面带有空格
     */
    private static final Pattern AT_USER_PATTERN = Pattern.compile("\\@(.+?)\\s");

    private Context context;

    private Message<?> message;

    private Handler handler = new Handler();

    public CustomTextView(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public CustomTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (StringUtils.isBlank(text)) {
            return;
        }

        // 把整个文本作为SpannableString，并在适当的位置设置span，用于展示表情和@的人
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        setEmojiSpan(text, type, ssb);
        setAtUserSpan(text, type, ssb);

        super.setText(ssb, type);
    }

    private void setEmojiSpan(CharSequence text, BufferType type, SpannableStringBuilder ssb) {
        // 针对正则匹配到的每个表情，设置表情图片span
        RegexUtil.matchRegex(text, EMOJI_PATTERN, ((matchedStr, start, end) -> {
            Log.i(LoggerName.UI, String.format("render emoji: (%d,%d) %s", start, end, matchedStr));

            FaceIconEmoji emoji = FaceIconManager.getInstance().findByEmojiContent(matchedStr);
            if (emoji == null) {
                return;
            }

            ImageUtil.asyncLoadImage(context, emoji.getLocalFilePath(), (bitmap) -> {
                // 创建表情图片span
                String emojiStr = emoji.getCn();
                ssb.setSpan(new EmojiSpan(context, bitmap, DynamicDrawableSpan.ALIGN_CENTER, emojiStr), start,
                        end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // 图片异步加载完成后，还要再设置一次文本，使图片span显示出来
                CustomTextView.super.setText(ssb, type);
            });
        }));
    }

    private void setAtUserSpan(CharSequence text, BufferType type, SpannableStringBuilder ssb) {
        if (message == null || CollectionUtils.isEmpty(message.getAtUsers())) {
            return;
        }

        // 查询@的人的群成员信息
        List<String> atUserIds = message.getAtUsers().stream().map(User::getUserId).collect(Collectors.toList());
        UserCacheManager.getInstance().batchQueryGroupMembers(message.getSid(), atUserIds,
                new ImCallback<List<GroupRelation>>() {
                    @Override
                    public void onSuccess(List<GroupRelation> data) {
                        handler.post(() -> {
                            doSetAtUserSpan(text, type, ssb, data);
                        });
                    }

                    @Override
                    public void onError(String errorCode, String m, Throwable t) {
                    }
                });
    }

    private void doSetAtUserSpan(CharSequence text, BufferType type, SpannableStringBuilder ssb,
                                 List<GroupRelation> members) {
        // 针对正则匹配到的每个@的人，设置高亮显示和点击事件，并展示对方已读状态
        RegexUtil.matchRegex(text, AT_USER_PATTERN, ((matchedStr, start, end) -> {
            Log.i(LoggerName.UI, String.format("render atUser: (%d,%d) %s", start, end, matchedStr));

            // 根据群成员信息，查找@的用户id
            Optional<String> atUserId = getAtUserId(matchedStr, members);

            // 如果未找到@的用户id(例如被@的用户已退群，或@后面跟的是普通字符而不是用户名)，则不处理
            if (!atUserId.isPresent()) {
                return;
            }

            // 创建@某人的span
            int flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
            ssb.setSpan(new AtUserSpan(atUserId.get(), null), start, end, flag);
            ssb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.ant_blue)), start, end, flag);

            // 如果是自己发送的消息，且不是@所有人，则需要展示对方已读状态
            if (MessageUtil.isSendByMe(message) && !StringUtils.equals(atUserId.get(),
                    CommonConstants.AT_ALL_USER_ID)) {
                // 获取该用户的已读状态
                boolean atUserRead = isAtUserRead(atUserId.get(), message.getAtUsers());

                // 创建对方已读状态的图片span，位置放在AtUserSpan的最后一个字符(原本是空格)
                ssb.setSpan(
                        new ImageSpan(context, atUserRead ? R.drawable.ic_at_user_read : R.drawable.ic_at_user_unread,
                                DynamicDrawableSpan.ALIGN_BASELINE), end - 1, end, flag);
            }

            // TODO span点击事件，待优化
            //ss.setSpan(new AtUserClickableSpan("Z002"), start, end, flag);
        }));

        // 使ClickableSpan的onClick事件生效
//        super.setMovementMethod(LinkMovementMethod.getInstance());
//        super.setFocusable(false);

        // 异步加载完成后，还要再设置一次文本，使span显示出来
        CustomTextView.super.setText(ssb, type);
    }

    private Optional<String> getAtUserId(String atUserStr, List<GroupRelation> members) {
        // 1. 从@某人的字符串中截取出用户名，"@aaa " -> "aaa"
        String atUserName = atUserStr.substring(1, atUserStr.length() - 1);

        if (StringUtils.equals(atUserName, "所有人")) {
            return Optional.of(CommonConstants.AT_ALL_USER_ID);
        }

        // 2. 根据群成员信息中的用户名关联到用户id
        // TODO 可能不准确(用户改名或重名)
        Optional<String> atUserId = members.stream().filter(m -> {
            String userName = StringUtils.defaultIfBlank(m.getUserNick(),
                    StringUtils.defaultIfBlank(m.getNickName(), m.getUserName()));
            return StringUtils.equals(userName, atUserName);
        }).map(GroupRelation::getUserId).findFirst();

        return atUserId;
    }

    private boolean isAtUserRead(String atUserId, List<User> atUsers) {
        // 1. 根据用户id关联到@某人的用户信息
        Optional<User> atUser = atUsers.stream().filter(
                u -> StringUtils.equals(u.getUserId(), atUserId)).findFirst();

        if (!atUser.isPresent() || atUser.get().getExtInfo() == null) {
            return false;
        }

        // 2. 从用户信息中获取已读状态
        Object atUserReadStatus = atUser.get().getExtInfo().get(Constants.AT_USER_READ_STATUS);
        return atUserReadStatus != null && (StringUtils.equals(atUserReadStatus.toString(),
                MessageReadStatusEnum.READ.getCode()));
    }

    public void setMessage(Message<?> message) {
        this.message = message;
    }

    private class AtUserClickableSpan extends ClickableSpan {

        private String atUserId;

        public AtUserClickableSpan(String atUserId) {
            this.atUserId = atUserId;
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setColor(getResources().getColor(R.color.ant_blue));
        }

        @Override
        public void onClick(@NonNull View widget) {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra(CommonConstants.USER_ID, atUserId);
            context.startActivity(intent);
        }
    }

}