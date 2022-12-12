/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.hsbcsd.mpaastest.R;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.GoodFriendVO;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListAdapter;

/**
 * 新的朋友列表
 *
 * @author liyalong
 * @version NewFriendListAdapter.java, v 0.1 2022年08月01日 20:11 liyalong
 */
public class NewFriendListAdapter extends BaseListAdapter<GoodFriendVO, NewFriendItemHolder> {

    public NewFriendListAdapter(Context context) {
        super(context);
    }

    @Override
    protected NewFriendItemHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_new_friend_item, parent, false);
        return new NewFriendItemHolder(context, view);
    }

}