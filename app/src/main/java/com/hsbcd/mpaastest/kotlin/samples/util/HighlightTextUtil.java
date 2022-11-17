/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 高亮文字工具类，用于渲染搜索结果
 *
 * @author liyalong
 * @version HighlightTextUtil.java, v 0.1 2022年08月09日 19:07 liyalong
 */
public class HighlightTextUtil {

    public static String convertText(String rawText) {
        if (StringUtils.isBlank(rawText)) {
            return StringUtils.EMPTY;
        }

        // 搜索结果里，命中关键字的部分，服务端返回时会带上高亮标记(<em>xx</em>)，这里转换为蓝色(R.color.ant_blue)加粗字体
        return rawText.replace("<em>", "<b><font color='#0091FF'>")
                .replace("</em>", "</font></b>");
    }
}