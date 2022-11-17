/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import cn.hsbcsd.mpaastest.R;
import com.alipay.fc.ccmimplus.common.service.facade.enums.SessionMetaTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.enums.SessionMemberListOpTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.model.SessionMemberItem;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.AddGroupMemberActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.CreateConversationActivity;
import com.hsbcd.mpaastest.kotlin.samples.util.GroupUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * 会话成员网格
 *
 * @author liyalong
 * @version SessionMemberGridAdapter.java, v 0.1 2022年08月10日 16:11 liyalong
 */
public class SessionMemberGridAdapter extends BaseAdapter {

    private Context context;

    private List<SessionMemberItem> members = Lists.newArrayList();

    private SessionMetaTypeEnum sessionType;

    public SessionMemberGridAdapter(Context context, SessionMetaTypeEnum sessionType) {
        this.context = context;
        this.sessionType = sessionType;
    }

    @Override
    public int getCount() {
        // 除群成员列表外，默认多加一个元素，用于放置添加成员按钮
        int additionalCount = 1;

        // 如果是群owner或管理员，再加一个元素，用于放置删除成员按钮
        if (sessionType == SessionMetaTypeEnum.GROUP || sessionType == SessionMetaTypeEnum.MEETING) {
            Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
            if (GroupUtil.isGroupAdmin(c)) {
                additionalCount++;
            }
        }

        return members.size() + additionalCount;
    }

    @Override
    public Object getItem(int position) {
        if (position < members.size()) {
            return members.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 返回添加成员的图标，需要同时展示添加和删除两个图标时，添加放在倒数第二个，否则放在倒数第一个
        if (((getCount() - members.size() == 1) && position == (getCount() - 1)) ||
                (getCount() - members.size() == 2) && position == (getCount() - 2)) {
            if (convertView == null) {
                convertView = getAddMemberItemView(parent);
            }
            return convertView;
        }

        // 返回删除成员的图标，需要同时展示添加和删除两个图标时，删除放在倒数第一个
        if ((getCount() - members.size() == 2) && position == (getCount() - 1)) {
            if (convertView == null) {
                convertView = getRemoveMemberItemView(parent);
            }
            return convertView;
        }

        SessionMemberGridItemHolder itemHolder = null;

        if (convertView == null || convertView.getTag() == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_member_grid_item,
                    parent, false);
            itemHolder = new SessionMemberGridItemHolder(context, convertView);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (SessionMemberGridItemHolder) convertView.getTag();
        }

        SessionMemberItem member = members.get(position);
        itemHolder.bind(member);

        return convertView;
    }

    private View getAddMemberItemView(ViewGroup parent) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.fragment_add_member_item, parent, false);

        // 点击添加成员时，跳转到选择用户列表页面
        // 如果是单聊，则创建群聊会话；如果是群聊，则添加群聊成员
        // TODO 跳转后默认选中当前已有成员，且不允许取消
        convertView.setOnClickListener(v -> {
            if (sessionType == SessionMetaTypeEnum.SINGLE) {
                Intent intent = new Intent(context, CreateConversationActivity.class);
                context.startActivity(intent);
            } else if (sessionType == SessionMetaTypeEnum.GROUP || sessionType == SessionMetaTypeEnum.MEETING) {
                Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
                if (!GroupUtil.canModifyGroup(c)) {
                    ToastUtil.makeToast((Activity) context, "无操作权限", 1000);
                    return;
                }

                Intent intent = new Intent(context, AddGroupMemberActivity.class);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    private View getRemoveMemberItemView(ViewGroup parent) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.fragment_remove_member_item, parent, false);

        // 点击删除成员时，跳转到群成员列表页面
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SessionMemberListActivity.class);
            intent.putExtra(SessionMemberListActivity.OP_TYPE_KEY, SessionMemberListOpTypeEnum.REMOVE_MEMBER.name());
            context.startActivity(intent);
        });

        return convertView;
    }

    public List<SessionMemberItem> getMembers() {
        return members;
    }

    public void setMembers(List<SessionMemberItem> members) {
        this.members = members;
    }

}