/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liyalong
 * @version LiveStreamUtil.java, v 0.1 2022年12月06日 14:17 liyalong
 */
public class LiveStreamUtil {

    /**
     * 阿里云直播推流/拉流地址格式：rtmp://DomainName/AppName/StreamName?auth_key=timestamp-rand-uid-md5hash
     * 详细参见：https://help.aliyun.com/document_detail/199349.htm
     * 这里把timestamp截取出来，检查是否过期
     *
     * @param url
     * @return
     */
    public static boolean isUrlExpired(String url) {
        String[] strArray = StringUtils.split(url, "=");
        if (strArray.length < 2) {
            return false;
        }

        String[] strArray2 = StringUtils.split(strArray[1], "-");
        if (strArray2.length < 4) {
            return false;
        }

        try {
            // 目前后端配置url过期时间为12小时
            long timestamp = Long.valueOf(strArray2[0]);
            return (System.currentTimeMillis() / 1000 - timestamp) > 60 * 60 * 12;
        } catch (Exception e) {
            return false;
        }
    }

}