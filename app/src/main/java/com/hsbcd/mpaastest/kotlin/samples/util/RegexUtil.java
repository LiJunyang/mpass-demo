/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liyalong
 * @version RegexUtil.java, v 0.1 2022年10月14日 15:38 liyalong
 */
public class RegexUtil {

    public static void matchRegex(CharSequence text, Pattern pattern, MatchRegexCallback callback) {
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String group = matcher.group();
            int start = matcher.start();
            int end = matcher.end();

            callback.onCallback(group, start, end);
        }
    }

    public interface MatchRegexCallback {
        void onCallback(String matchedStr, int start, int end);
    }

}