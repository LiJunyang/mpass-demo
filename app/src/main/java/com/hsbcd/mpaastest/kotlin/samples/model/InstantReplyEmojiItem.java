/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.model;

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.alipay.fc.ccmimplus.sdk.core.config.face.FaceIconEmoji;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author liyalong
 * @version InstantReplyEmojiItem.java, v 0.1 2022年09月22日 20:06 liyalong
 */
public class InstantReplyEmojiItem {

    /**
     * 表情
     */
    private FaceIconEmoji emoji;

    /**
     * 发送该快捷回复表情的用户列表
     */
    private List<UserInfoVO> senderUserInfoVOs = Lists.newArrayList();

    /**
     * 判断指定用户是否已发送了该表情
     *
     * @param userId
     * @return
     */
    public boolean hasSentEmoji(String userId) {
        for (UserInfoVO userInfoVO : senderUserInfoVOs) {
            if (StringUtils.equals(userInfoVO.getUserId(), userId)) {
                return true;
            }
        }

        return false;
    }

    public FaceIconEmoji getEmoji() {
        return emoji;
    }

    public void setEmoji(FaceIconEmoji emoji) {
        this.emoji = emoji;
    }

    public List<UserInfoVO> getSenderUserInfoVOs() {
        return senderUserInfoVOs;
    }

    public void setSenderUserInfoVOs(
            List<UserInfoVO> senderUserInfoVOs) {
        this.senderUserInfoVOs = senderUserInfoVOs;
    }
}