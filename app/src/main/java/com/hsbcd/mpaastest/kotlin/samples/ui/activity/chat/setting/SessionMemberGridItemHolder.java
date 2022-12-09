/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;

import cn.com.hsbc.hsbcchina.cert.R;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import cn.com.hsbc.hsbcchina.cert.databinding.SessionMemberGridItemBinding;
import com.hsbcd.mpaastest.kotlin.samples.model.SessionMemberItem;
import com.alipay.fc.ccmimplus.sdk.core.enums.GroupMemberRoleEnum;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts.UserDetailActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.apache.commons.lang3.StringUtils;

/**
 * 会话成员项
 *
 * @author liyalong
 * @version SessionMemberItemHolder.java, v 0.1 2022年08月10日 16:11 liyalong
 */
public class SessionMemberGridItemHolder {

    public Context context;

    private SessionMemberGridItemBinding binding;

    public SessionMemberGridItemHolder(Context context, View itemView) {
        this.context = context;
        binding = SessionMemberGridItemBinding.bind(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void bind(SessionMemberItem member) {
        Glide.with(context).load(
                StringUtils.defaultIfBlank(member.getUserAvatar(), CommonConstants.DEFAULT_AVATAR)).diskCacheStrategy(
                DiskCacheStrategy.ALL).into(binding.userAvatar);

        // 如果是群主或群管理员，在头像上显示相应标记
        if (StringUtils.equals(member.getRole(), GroupMemberRoleEnum.OWNER.name())) {
            binding.userAvatar.setForeground(context.getDrawable(R.drawable.ic_group_owner));
        } else if (StringUtils.equals(member.getRole(), GroupMemberRoleEnum.ADMIN.name())) {
            binding.userAvatar.setForeground(context.getDrawable(R.drawable.ic_group_admin));
        }

        binding.userName.setText(member.getUserName());

        binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra(CommonConstants.USER_ID, member.getUserId());
            context.startActivity(intent);
        });
    }

}