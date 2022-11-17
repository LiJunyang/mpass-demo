/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import cn.hsbcsd.mpaastest.R;
import com.hsbcd.mpaastest.kotlin.samples.enums.SessionMemberListOpTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.model.SessionMemberItem;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.enums.GroupMemberRoleEnum;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListAdapter;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话成员列表
 *
 * @author liyalong
 * @version SessionMemberListAdapter.java, v 0.1 2022年08月15日 10:11 liyalong
 */
public class SessionMemberListAdapter extends BaseListAdapter<SessionMemberItem, SessionMemberListItemHolder> {

    private SessionMemberListOpTypeEnum opType;

    public SessionMemberListAdapter(Context context, SessionMemberListOpTypeEnum opType,
                                    BaseListItemHolder.OnItemActionListener<SessionMemberItem> onItemActionListener) {
        super(context, onItemActionListener);
        this.opType = opType;
    }

    @Override
    protected SessionMemberListItemHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_member_list_item, parent,
                false);
        return new SessionMemberListItemHolder(context, view, onItemActionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionMemberListItemHolder holder, int position) {
        holder.bind(items.get(position), opType);
    }

    @Override
    public void setItems(List<SessionMemberItem> members) {
        this.items = filterMembers(members);
    }

    @Override
    public void addItem(SessionMemberItem member) {
        this.items.addAll(filterMembers(Arrays.asList(member)));
    }

    @Override
    public void addAll(List<SessionMemberItem> members) {
        this.items.addAll(filterMembers(members));
    }

    private List<SessionMemberItem> filterMembers(List<SessionMemberItem> members) {
        List<SessionMemberItem> filteredMembers = null;

        switch (opType) {
            // 如果是删除群成员/新增管理员/新增禁言成员，渲染列表时排除掉群主和管理员
            case REMOVE_MEMBER:
            case ADD_ADMIN:
            case ADD_MUTE_MEMBER: {
                filteredMembers = members.stream().filter(m -> {
                    GroupMemberRoleEnum role = GroupMemberRoleEnum.valueOf(m.getRole());
                    return role != GroupMemberRoleEnum.OWNER && role != GroupMemberRoleEnum.ADMIN;
                }).collect(Collectors.toList());
                break;
            }
            // 如果是展示管理员列表，渲染列表时只取管理员
            case LIST_ADMIN: {
                filteredMembers = members.stream().filter(m -> {
                    GroupMemberRoleEnum role = GroupMemberRoleEnum.valueOf(m.getRole());
                    return role == GroupMemberRoleEnum.ADMIN;
                }).collect(Collectors.toList());
                break;
            }
            // 如果是转让群主，渲染列表时排除掉群主
            case TRANSFER_OWNER: {
                filteredMembers = members.stream().filter(m -> {
                    GroupMemberRoleEnum role = GroupMemberRoleEnum.valueOf(m.getRole());
                    return role != GroupMemberRoleEnum.OWNER;
                }).collect(Collectors.toList());
                break;
            }
            // 如果是@单个/多个群成员，渲染列表时排除掉自己
            case AT_SINGLE_USER:
            case AT_MULTI_USER: {
                filteredMembers = members.stream().filter(m -> !StringUtils.equals(m.getUserId(),
                        AlipayCcmIMClient.getInstance().getCurrentUserId())).collect(Collectors.toList());
                break;
            }
            // 其他情况默认渲染全量列表
            default: {
                filteredMembers = members;
                break;
            }
        }

        return filteredMembers;
    }

    public void setOpType(SessionMemberListOpTypeEnum opType) {
        this.opType = opType;
    }

}