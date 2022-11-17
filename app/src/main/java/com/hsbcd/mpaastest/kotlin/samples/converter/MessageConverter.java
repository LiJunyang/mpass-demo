/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.converter;

import android.os.Handler;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.InstantReplyFaceInfo;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.model.InstantReplyEmojiItem;
import com.alipay.fc.ccmimplus.sdk.core.config.face.FaceIconManager;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 消息模型转换器
 *
 * @author liyalong
 * @version MessageConverter.java, v 0.1 2022年08月09日 18:53 liyalong
 */
public class MessageConverter {

    private static Handler handler = new Handler();

    /**
     * 消息的快捷回复表情列表转换为按表情分组的列表，用于消息渲染
     * <p>
     * 输入：
     * 1. emoji_1: user_id_1
     * 2. emoji_2: user_id_2
     * 3. emoji_1: user_id_3
     * <p>
     * 输出：
     * 1. emoji_1: user_name_1, user_name_3
     * 2. emoji_2: user_name_2
     *
     * @param faceInfos
     * @return
     */
    public static List<InstantReplyEmojiItem> convertInstantReplyEmojiList(List<InstantReplyFaceInfo> faceInfos,
                                                                           Map<String, UserInfoVO> senderUserInfoMap) {
        List<InstantReplyEmojiItem> emojiItems = Lists.newArrayList();

        if (CollectionUtils.isEmpty(faceInfos)) {
            return emojiItems;
        }

        // 按表情分组
        Map<String, Set<String>> emojiId2UserIdMap = Maps.newHashMap();
        faceInfos.forEach(info -> {
            String emojiId = info.getFaceContent().getIconId();
            Set<String> userIds = emojiId2UserIdMap.get(emojiId);

            if (userIds == null) {
                userIds = Sets.newLinkedHashSet();
            }

            userIds.add(info.getFrom().getUid());
            emojiId2UserIdMap.put(emojiId, userIds);
        });

        // 组装结果
        emojiId2UserIdMap.forEach((emojiId, userIds) -> {
            InstantReplyEmojiItem emojiItem = new InstantReplyEmojiItem();

            emojiItem.setEmoji(FaceIconManager.getInstance().findByEmojiId(emojiId));
            userIds.forEach(userId -> emojiItem.getSenderUserInfoVOs().add(senderUserInfoMap.get(userId)));

            emojiItems.add(emojiItem);
        });

        return emojiItems;
    }

}