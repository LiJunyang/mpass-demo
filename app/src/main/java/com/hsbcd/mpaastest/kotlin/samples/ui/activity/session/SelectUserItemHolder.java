/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.SelectUserViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.SelectUserListActivity;

import org.apache.commons.lang3.StringUtils;

import cn.com.hsbc.hsbcchina.cert.databinding.FragmentSelectUsersItemBinding;

/**
 * @author liyalong
 * @version SelectUserItemHolder.java, v 0.1 2022年08月01日 20:57 liyalong
 */
public class SelectUserItemHolder extends BaseListItemHolder<UserInfoVO> {

    private FragmentSelectUsersItemBinding binding;

    private SelectUserViewModel selectItemViewModel;

    public SelectUserItemHolder(Context context, View itemView) {
        super(context, itemView);

        binding = FragmentSelectUsersItemBinding.bind(itemView);

        // 绑定选择用户数据
        selectItemViewModel = new ViewModelProvider((AppCompatActivity) context).get(SelectUserViewModel.class);
    }

    @Override
    public void bind(UserInfoVO userInfoVO) {
        Glide.with(context).load(StringUtils.defaultIfBlank(userInfoVO.getAvatarUrl(),
                CommonConstants.DEFAULT_AVATAR)).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.userAvatar);

        binding.userName.setText(StringUtils.defaultIfBlank(userInfoVO.getNickName(), userInfoVO.getUserName()));

        if (context instanceof SelectUserListActivity) {
            // 单选模式
            if (((SelectUserListActivity) context).isSingleSelectMode()) {
                binding.selectUserCheckBox.setVisibility(View.GONE);

                // 绑定单个用户的点击事件
                binding.userInfoLayout.setOnClickListener(
                        v -> ((SelectUserListActivity) context).onClickSingleUser(userInfoVO));
            }
            // 多选模式
            else {
                binding.selectUserCheckBox.setVisibility(View.VISIBLE);
                binding.selectUserCheckBox.setOnCheckedChangeListener(null);
                binding.selectUserCheckBox.setChecked(selectItemViewModel.isSelected(userInfoVO));
                binding.selectUserCheckBox.setOnCheckedChangeListener((v, b) -> onCheckedSelectUser(userInfoVO, b));
            }
        } else {
            binding.selectUserCheckBox.setVisibility(View.GONE);
        }
    }

    private void onCheckedSelectUser(UserInfoVO data, boolean checked) {
        // 更新选中的用户id列表，用于页面刷新已选中人数
        selectItemViewModel.updateSelectedItems(data, checked);
    }

}