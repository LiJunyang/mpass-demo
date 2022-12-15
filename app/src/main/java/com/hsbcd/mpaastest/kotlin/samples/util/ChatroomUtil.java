/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import com.alipay.fc.ccmimplus.common.service.facade.enums.ChatroomRoleEnum;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;

/**
 * 聊天室会话工具类
 *
 * @author liyalong
 * @version ChatroomUtil.java, v 0.1 2022年11月11日 15:58 liyalong
 */
public class ChatroomUtil {

    public static boolean isChatroomOwner(Conversation c) {
        if (c == null || c.getChatroom() == null || c.getChatroom().getCurrentMember() == null) {
            return false;
        }

        return c.getChatroom().getCurrentMember().getRoleId() == ChatroomRoleEnum.OWNER.codeAsInt();

//        if (c == null || c.getChatroom() == null) {
//            return false;
//        }
//
//        return StringUtils.equals(c.getChatroom().getOwnerId(), AlipayCcmIMClient.getInstance().getCurrentUserId());
    }

}