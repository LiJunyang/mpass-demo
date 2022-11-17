/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.model.VisitingCardData;

/**
 * 发送名片页
 *
 * @author liyalong
 * @version SendVisitingCardActivity.java, v 0.1 2022年08月022日 22:15 liyalong
 */
public class SendVisitingCardActivity extends SelectUserListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding.selectUserLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean isSingleSelectMode() {
        return true;
    }

    @Override
    public void onClickSingleUser(UserInfoVO userInfoVO) {
        Intent intent = new Intent();
        intent.putExtra("visitingCardData", buildVisitingCardData(userInfoVO));

        setResult(RESULT_OK, intent);
        finish();
    }

    private VisitingCardData buildVisitingCardData(UserInfoVO userInfoVO) {
        VisitingCardData data = new VisitingCardData();

        data.setUserId(userInfoVO.getUserId());
        data.setUserAvatar(userInfoVO.getAvatarUrl());
        data.setUserName(userInfoVO.getUserName());
        data.setNickName(userInfoVO.getNickName());

        return data;
    }

}