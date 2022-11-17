/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import com.alipay.fc.ccmimplus.sdk.core.enums.GroupMemberRoleEnum;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;

/**
 * 会话工具类
 *
 * @author liyalong
 * @version ConversationUtil.java, v 0.1 2022年08月11日 16:46 liyalong
 */
public class GroupUtil {

    /**
     * 当前用户是否为群主
     *
     * @param c
     * @return
     */
    public static boolean isGroupOwner(Conversation c) {
        if (c == null || c.getGroup() == null || c.getGroup().getRelationOfMe() == null) {
            return false;
        }

        GroupMemberRoleEnum role = c.getGroup().getRelationOfMe().getRole();
        return role == GroupMemberRoleEnum.OWNER;
    }

    /**
     * 当前用户是否为群主或管理员
     *
     * @param c
     * @return
     */
    public static boolean isGroupAdmin(Conversation c) {
        if (c == null || c.getGroup() == null || c.getGroup().getRelationOfMe() == null) {
            return false;
        }

        GroupMemberRoleEnum role = c.getGroup().getRelationOfMe().getRole();
        return (role == GroupMemberRoleEnum.OWNER || role == GroupMemberRoleEnum.ADMIN);
    }

    /**
     * 当前用户是否为普通成员
     *
     * @param c
     * @return
     */
    public static boolean isNormalMember(Conversation c) {
        return !isGroupAdmin(c);
    }

    /**
     * 是否仅群主和管理员有群信息修改权限
     *
     * @param c
     * @return
     */
    public static boolean isOnlyAllowAdminModify(Conversation c) {
        if (c == null || c.getBizParams() == null) {
            return false;
        }

        return c.getBizParams().getBoolean("isAllowAdminModify");
    }

    /**
     * 当前用户是否可修改群信息
     *
     * @param c
     * @return
     */
    public static boolean canModifyGroup(Conversation c) {
        // 群主和群管理员默认有权限
        if (GroupUtil.isGroupAdmin(c)) {
            return true;
        }

        // 普通成员要看当前群设置是否开放了权限
        return !isOnlyAllowAdminModify(c);
    }

}