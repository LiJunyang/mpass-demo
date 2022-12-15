/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.common;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alipay.fc.ccmimplus.common.service.facade.enums.SessionMemberChangeOpTypeEnum;

import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.conversation.GroupConversationListener;
import com.alipay.fc.ccmimplus.sdk.core.enums.GroupUpdateTypeEnum;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Group;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.GroupRelation;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;

import java.util.List;

/**
 * @author liyalong
 * @version NotifyViewModel.java, v 0.1 2022年11月02日 16:48 liyalong
 */
public class NotifyViewModel extends ViewModel implements GroupConversationListener {

    public NotifyViewModel() {
        AlipayCcmIMClient.getInstance().getConversationManager().registerGroupConversationListener(this);
    }

    @Override
    public void onCreateGroup(Conversation c, Group group) {

    }

    @Override
    public void onUpdateGroup(Group group, GroupUpdateTypeEnum updateType) {
        Log.i(LoggerName.VIEW_MODEL, String.format("onUpdateGroup [%s]: %s", updateType,
                JSONObject.toJSONString(group, SerializerFeature.PrettyFormat)));
    }

    @Override
    public void onDisMissGroup(Group group) {

    }

    @Override
    public void onAddMember(Group group, List<GroupRelation> members) {

    }

    @Override
    public void onRemoveMember(Group group, List<GroupRelation> members) {

    }

    @Override
    public void onUpdateGroupMember(Group group, SessionMemberChangeOpTypeEnum updateType,
                                    List<GroupRelation> members) {

    }
}