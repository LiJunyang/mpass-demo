/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.alipay.fc.ccmimplus.common.service.facade.enums.AddGoodFriendReplyStatusEnum;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.GoodFriendVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;

import org.apache.commons.lang3.StringUtils;

import cn.com.hsbc.hsbcchina.cert.databinding.FragmentNewFriendItemBinding;

/**
 * @author liyalong
 * @version NewFriendItemHolder.java, v 0.1 2022年08月01日 20:57 liyalong
 */
public class NewFriendItemHolder extends BaseListItemHolder<GoodFriendVO> {

    private FragmentNewFriendItemBinding binding;

    private ContactsViewModel contactsViewModel;

    public NewFriendItemHolder(Context context, View itemView) {
        super(context, itemView);

        binding = FragmentNewFriendItemBinding.bind(itemView);

        // 绑定联系人数据
        contactsViewModel = new ViewModelProvider((NewFriendActivity) context).get(ContactsViewModel.class);
    }

    @Override
    public void bind(GoodFriendVO friendVO) {
        Glide.with(context).load(
                StringUtils.defaultIfBlank(friendVO.getAvatarUrl(), CommonConstants.DEFAULT_AVATAR)).diskCacheStrategy(
                DiskCacheStrategy.ALL).into(binding.userAvatar);

        binding.userName.setText(StringUtils.defaultIfBlank(friendVO.getNickName(), friendVO.getUserName()));
        binding.userId.setText(String.format("ID: %s", friendVO.getInvitedUserId()));

        binding.newFriendLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra(CommonConstants.USER_ID, friendVO.getInvitedUserId());
            context.startActivity(intent);
        });

        bindStatus(friendVO);
    }

    private void bindStatus(GoodFriendVO friendVO) {
        String currentUserId = AlipayCcmIMClient.getInstance().getCurrentUserId();
        AddGoodFriendReplyStatusEnum status = AddGoodFriendReplyStatusEnum.getByCode(
                String.valueOf(friendVO.getStatus()));

        binding.agreeButton.setVisibility(View.GONE);
        binding.statusLabel.setVisibility(View.VISIBLE);

        // 自己发起的添加好友请求
        if (StringUtils.equals(currentUserId, friendVO.getOriginnator())) {
            switch (status) {
                case AGREE: {
                    binding.statusLabel.setText("Added");
                    break;
                }
                case REJECT: {
                    binding.statusLabel.setText("Rejected");
                    break;
                }
                case EXPIRED: {
                    binding.statusLabel.setText("Expired");
                    break;
                }
                case WAIT: {
                    binding.statusLabel.setText("Pending");
                    break;
                }
                default:
                    break;
            }
        }
        // 别人发给自己的添加好友请求
        else {
            switch (status) {
                case AGREE: {
                    binding.statusLabel.setText("Added");
                    break;
                }
                case REJECT: {
                    binding.statusLabel.setText("Rejected");
                    break;
                }
                case EXPIRED: {
                    binding.statusLabel.setText("Expired");
                    break;
                }
                case WAIT: {
                    binding.agreeButton.setVisibility(View.VISIBLE);
                    binding.statusLabel.setVisibility(View.GONE);

                    binding.agreeButton.setOnClickListener(v -> {
                        contactsViewModel.replyAddFriend(friendVO.getInvitedNo(), friendVO.getInvitedUserId(), true);

                        binding.agreeButton.setVisibility(View.GONE);
                        binding.statusLabel.setVisibility(View.VISIBLE);
                        binding.statusLabel.setText("Added");
                    });
                    
                    break;
                }
                default:
                    break;
            }
        }
    }
}