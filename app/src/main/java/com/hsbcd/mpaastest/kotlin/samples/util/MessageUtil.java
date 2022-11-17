/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import com.alipay.fc.ccmimplus.common.message.domain.MessageContentType;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.FaceContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.LocationContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.TextContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.UrlContent;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.MessageVO;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.GroupRelation;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.UserCacheManager;

import org.apache.commons.lang3.StringUtils;

/**
 * 消息工具类
 *
 * @author liyalong
 * @version MessageUtil.java, v 0.1 2022年09月01日 15:44 liyalong
 */
public class MessageUtil {

    /**
     * 获取消息内容文本
     *
     * @param message
     * @return
     */
    public static String getContentText(Message message) {
        return getContentText(message.getContentTypeCode(), message.getContent());
    }

    public static String getContentText(int contentTypeCode, Object content) {
        MessageContentType type = MessageContentType.TEXT;
        try {
            type = MessageContentType.forNumber(contentTypeCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        switch (type) {
            case TEXT:
            case FACE:
                return getTextContent(content);

            case IMAGE:
                return "[图片]";

            case VOICE:
                return "[语音]";

            case VIDEO:
                return "[视频]";

            case RTC:
                return "[实时通话]";

            case FILE:
                return "[文件]";

            case LOCATION:
                return "[位置]";

            case URL:
                return "[链接]";

            case CUSTOM:
                return "[自定义消息]";

            case CARD:
                return "[卡片消息]";

            case RICH_TEXT:
                return "[富文本]";

            case MERGED:
                return "[聊天记录]";

            default:
                return "[未知消息类型]";
        }
    }

    private static String getTextContent(Object content) {
        if (content != null) {
            // 文本消息
            if (MessageVO.TextMessageVO.class.isInstance(content)) {
                MessageVO.TextMessageVO textMsg = (MessageVO.TextMessageVO) content;
                return textMsg.getText();
            }

            if (content instanceof TextContent) {
                return ((TextContent) content).getText();
            }

            // 表情消息
            if (content instanceof FaceContent) {
                return ((FaceContent) content).getEmoji();
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取消息内容文本
     *
     * @param message
     * @return
     */
    public static String getContentTextForCopy(Message message) {
        if (message == null) {
            return StringUtils.EMPTY;
        }

        Object content = message.getContent();

        // 文本
        if (content instanceof TextContent) {
            return ((TextContent) content).getText();
        } else if (content instanceof MessageVO.TextMessageVO) {
            return ((MessageVO.TextMessageVO) content).getText();
        }
        // 链接url
        else if (content instanceof UrlContent) {
            return ((UrlContent) content).getHref();
        }
        // 定位地址
        else if (content instanceof LocationContent) {
            return ((LocationContent) content).getTitle();
        }
        // 其他类型均不支持
        else {
            return StringUtils.EMPTY;
        }
    }

    public static String getSenderUserName(Conversation c, Message message) {
        String senderUserName;

        String senderUid = message.getFrom().getUid();

        if (StringUtils.equals(senderUid, AlipayCcmIMClient.getInstance().getCurrentUserId())) {
            UserInfoVO currentUser = UserCacheManager.getInstance().getCurrentUserInfo();
            senderUserName = StringUtils.defaultIfBlank(currentUser.getNickName(), currentUser.getUserName());
            return senderUserName;
        }

        if (c.isGroupConversation()) {
            GroupRelation sender = UserCacheManager.getInstance().syncGetGroupMember(c.getCid(), senderUid);
            senderUserName = StringUtils.defaultIfBlank(sender.getUserNick(),
                    StringUtils.defaultIfBlank(sender.getNickName(), sender.getUserName()));
        } else {
            UserInfoVO sender = UserCacheManager.getInstance().syncGetUserInfo(senderUid);
            senderUserName = StringUtils.defaultIfBlank(sender.getNickName(), sender.getUserName());
        }

        return senderUserName;
    }

    public static String getSenderConnectId(Message message) {
        return (String) message.getExtInfo().get("connectId");
    }

    public static boolean isSendByMe(Message message) {
        String senderUid = message.getFrom().getUid();
        return StringUtils.equals(senderUid, AlipayCcmIMClient.getInstance().getCurrentUserId());
    }

}