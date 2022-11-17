/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.converter;

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.hsbcd.mpaastest.kotlin.samples.model.SessionMemberItem;
import com.alipay.fc.ccmimplus.sdk.core.enums.GroupMemberRoleEnum;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.GroupRelation;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author liyalong
 * @version SessionMemberConverter.java, v 0.1 2022年08月10日 17:18 liyalong
 */
public class SessionMemberConverter {

    public static SessionMemberItem convertUser(UserVO user) {
        SessionMemberItem member = new SessionMemberItem();

        member.setUserId(user.getUserId());
        member.setUserAvatar(user.getAvatarUrl());
        member.setUserName(StringUtils.defaultIfBlank(user.getNickName(), user.getUserName()));

        return member;
    }

    public static List<SessionMemberItem> convertGroupRelations(List<GroupRelation> groupRelations) {
        List<SessionMemberItem> members = Lists.newArrayList();

        for (GroupRelation groupRelation : groupRelations) {
            members.add(convertGroupRelation(groupRelation));
        }

        return members;
    }

    public static SessionMemberItem convertGroupRelation(GroupRelation groupRelation) {
        SessionMemberItem member = new SessionMemberItem();

        member.setUserId(groupRelation.getUserId());
        member.setUserAvatar(groupRelation.getAvatarUrl());
        member.setUserName(StringUtils.defaultIfBlank(groupRelation.getUserNick(),
                StringUtils.defaultIfBlank(groupRelation.getNickName(), groupRelation.getUserName())));
        member.setRole(groupRelation.getRole().name());

        return member;
    }

    /**
     * 创建一个表示@所有人的成员列表项
     *
     * @return
     */
    public static SessionMemberItem createAtAllMemberItem(Conversation c) {
        SessionMemberItem member = new SessionMemberItem();

        member.setUserId(CommonConstants.AT_ALL_USER_ID);
        member.setUserAvatar(CommonConstants.DEFAULT_AVATAR);
        //member.setUserName(String.format("所有人 (%d)", c.getGroup().getMemberCount()));
        member.setUserName("所有人");
        member.setRole(GroupMemberRoleEnum.NORMAL.name());

        return member;
    }

    public static UserInfoVO convertUserInfo(SessionMemberItem member) {
        UserInfoVO userInfoVO = new UserInfoVO();

        userInfoVO.setUserId(member.getUserId());
        userInfoVO.setAvatarUrl(member.getUserAvatar());
        userInfoVO.setUserName(member.getUserName());

        return userInfoVO;
    }
}