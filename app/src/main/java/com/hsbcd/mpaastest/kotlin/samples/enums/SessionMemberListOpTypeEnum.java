/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.enums;

/**
 * 会话成员列表的操作类型枚举
 *
 * @author liyalong
 * @version SessionMemberListOpTypeEnum.java, v 0.1 2022年09月15日 16:35 liyalong
 */
public enum SessionMemberListOpTypeEnum {

    /**
     * 展示所有成员列表
     */
    LIST_ALL_MEMBER,

    /**
     * 删除成员
     */
    REMOVE_MEMBER,

    /**
     * 展示管理员列表
     */
    LIST_ADMIN,

    /**
     * 新增管理员
     */
    ADD_ADMIN,

    /**
     * 展示禁言成员列表
     */
    LIST_MUTE_MEMBER,

    /**
     * 新增禁言成员
     */
    ADD_MUTE_MEMBER,

    /**
     * 转让群主
     */
    TRANSFER_OWNER,

    /**
     * \@单个群成员
     */
    AT_SINGLE_USER,

    /**
     * \@多个群成员
     */
    AT_MULTI_USER,
    
    ;
}