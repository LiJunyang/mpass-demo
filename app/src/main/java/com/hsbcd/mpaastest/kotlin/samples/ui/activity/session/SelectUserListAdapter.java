/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.com.hsbc.hsbcchina.cert.R;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListAdapter;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author liyalong
 * @version SelectUserListAdapter.java, v 0.1 2022年08月01日 20:11 liyalong
 */
public class SelectUserListAdapter extends BaseListAdapter<UserInfoVO, SelectUserItemHolder> {

    public SelectUserListAdapter(Context context) {
        super(context);
    }

    @Override
    protected SelectUserItemHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_select_users_item, parent,
                false);
        return new SelectUserItemHolder(context, view);
    }

    public void addAll(List<UserInfoVO> userInfos, boolean excludeSelf) {
        if (!excludeSelf) {
            addAll(userInfos);
            return;
        }

        String currentUserId = AlipayCcmIMClient.getInstance().getCurrentUserId();
        for (UserInfoVO userInfoVO : userInfos) {
            if (!StringUtils.equals(userInfoVO.getUserId(), currentUserId)) {
                addItem(userInfoVO);
            }
        }
    }

}