/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import com.alipay.fc.ccmimplus.sdk.core.util.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author liyalong
 * @version DateUtil.java, v 0.1 2022年08月08日 16:35 liyalong
 */
public class DateUtil {

    public static String getMessageSendTime(long timestamp) {
        Date sendDate = new Date(timestamp);
        Date now = new Date();
        Date today = DateUtil.getDayBegin(now);

        String timeFormat = sendDate.before(today) ? "MM/dd" : "HH:mm";
        return DateUtils.formatDate(timestamp, timeFormat);
    }
    
    private static Date getDayBegin(Date date) {
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        df.setLenient(false);

        String dateString = df.format(date);

        try {
            return df.parse(dateString);
        } catch (ParseException e) {
            return date;
        }
    }

}