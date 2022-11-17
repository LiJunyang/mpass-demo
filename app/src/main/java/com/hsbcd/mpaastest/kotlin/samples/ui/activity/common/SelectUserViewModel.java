/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.common;

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;

/**
 * @author liyalong
 * @version SelectUserViewModel.java, v 0.1 2022年08月30日 14:19 liyalong
 */
public class SelectUserViewModel extends SelectItemViewModel<UserInfoVO> {

    @Override
    protected String getUniqueKey(UserInfoVO item) {
        return item.getUserId();
    }
}