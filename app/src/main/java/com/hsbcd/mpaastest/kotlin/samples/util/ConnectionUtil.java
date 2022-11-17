/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import com.alipay.fc.ccmimplus.sdk.core.connection.Connection;
import com.alipay.fc.ccmimplus.sdk.core.connection.ConnectionManager;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liyalong
 * @version ConnectionUtil.java, v 0.1 2022年09月28日 18:05 liyalong
 */
public class ConnectionUtil {

    public static String getCurrentConnectionId() {
        Connection current = ConnectionManager.getInstance().getCurrentConnection();
        
        if (current == null) {
            return StringUtils.EMPTY;
        }

        return StringUtils.defaultIfBlank(current.getRemoteConnectId(), StringUtils.EMPTY);
    }

}