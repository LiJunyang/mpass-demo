/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting;

import android.content.Context;
import android.text.Html;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.hsbcd.mpaastest.kotlin.samples.converter.SessionMemberConverter;
import cn.hsbcsd.mpaastest.databinding.SessionMemberListItemBinding;
import com.hsbcd.mpaastest.kotlin.samples.enums.SessionMemberListOpTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.model.SessionMemberItem;
import com.alipay.fc.ccmimplus.sdk.core.enums.GroupMemberRoleEnum;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.SelectUserViewModel;
import com.hsbcd.mpaastest.kotlin.samples.util.HighlightTextUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liyalong
 * @version SessionMemberListItemHolder.java, v 0.1 2022年08月15日 09:57 liyalong
 */
public class SessionMemberListItemHolder extends BaseListItemHolder<SessionMemberItem> {

    private SessionMemberListItemBinding binding;

    private SelectUserViewModel selectItemViewModel;

    public SessionMemberListItemHolder(Context context, View itemView,
                                       OnItemActionListener onItemActionListener) {
        super(context, itemView, onItemActionListener);

        binding = SessionMemberListItemBinding.bind(itemView);

        // 绑定选择用户数据
        selectItemViewModel = new ViewModelProvider((AppCompatActivity) context).get(SelectUserViewModel.class);
    }

    @Override
    public void bind(SessionMemberItem item) {

    }

    public void bind(SessionMemberItem member, SessionMemberListOpTypeEnum opType) {
        Glide.with(context).load(member.getUserAvatar()).diskCacheStrategy(
                DiskCacheStrategy.ALL).into(binding.userAvatar);

        String userName = member.getUserName();
        binding.userName.setText(Html.fromHtml(HighlightTextUtil.convertText(userName)));

        if (StringUtils.equals(member.getUserId(), CommonConstants.AT_ALL_USER_ID)) {
            binding.userId.setText(StringUtils.EMPTY);
        } else {
            binding.userId.setText(String.format("ID: %s", member.getUserId()));
        }

        GroupMemberRoleEnum role = GroupMemberRoleEnum.valueOf(member.getRole());
        switch (role) {
            case OWNER:
                binding.role.setText("Owner");
                break;
            case ADMIN:
                binding.role.setText("Group Admin");
                break;
            default:
                binding.role.setText(StringUtils.EMPTY);
                break;
        }

        // 展示/隐藏多选框
        if (isMultiSelectMode(opType)) {
            UserInfoVO userInfoVO = SessionMemberConverter.convertUserInfo(member);

            binding.selectMemberCheckBox.setVisibility(View.VISIBLE);
            binding.selectMemberCheckBox.setOnCheckedChangeListener(null);
            binding.selectMemberCheckBox.setChecked(selectItemViewModel.isSelected(userInfoVO));
            binding.selectMemberCheckBox.setOnCheckedChangeListener((v, b) -> onCheckedSelectUser(userInfoVO, b));
        } else {
            binding.selectMemberCheckBox.setVisibility(View.GONE);
        }

        // 展示/隐藏移除按钮和角色标签
        if (canRemoveMember(opType)) {
            binding.removeButton.setVisibility(View.VISIBLE);
            binding.role.setVisibility(View.GONE);
        } else {
            binding.removeButton.setVisibility(View.GONE);
            binding.role.setVisibility(View.VISIBLE);
        }

        // 绑定成员动作
        if (onItemActionListener != null) {
            binding.userInfoLayout.setOnClickListener(v -> {
                // 仅单选模式下绑定成员单击事件
                if (!isMultiSelectMode(opType)) {
                    onItemActionListener.onClickItem(member);
                }
            });

            binding.removeButton.setOnClickListener(
                    v -> onItemActionListener.onRemoveItem(member, getBindingAdapterPosition()));
        }
    }

    private void onCheckedSelectUser(UserInfoVO userInfoVO, boolean checked) {
        // 更新选中的用户id列表，用于页面刷新已选中人数
        selectItemViewModel.updateSelectedItems(userInfoVO, checked);
    }

    private boolean isMultiSelectMode(SessionMemberListOpTypeEnum opType) {
        // 如果是删除群成员/新增群管理员/新增禁言成员/@多个群成员，则进入多选模式
        return opType == SessionMemberListOpTypeEnum.REMOVE_MEMBER || opType == SessionMemberListOpTypeEnum.ADD_ADMIN
                || opType == SessionMemberListOpTypeEnum.ADD_MUTE_MEMBER
                || opType == SessionMemberListOpTypeEnum.AT_MULTI_USER;
    }

    private boolean canRemoveMember(SessionMemberListOpTypeEnum opType) {
        // 如果是展示群管理员列表/展示禁言成员列表，则允许删除成员
        return opType == SessionMemberListOpTypeEnum.LIST_ADMIN || opType == SessionMemberListOpTypeEnum.LIST_MUTE_MEMBER;
    }

}