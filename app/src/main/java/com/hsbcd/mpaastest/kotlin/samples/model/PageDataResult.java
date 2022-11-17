/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.model;

/**
 * 分页列表数据结果
 *
 * @author liyalong
 * @version PageDataResult.java, v 0.1 2022年08月29日 11:04 liyalong
 */
public class PageDataResult<T> extends LiveDataResult<T> {

    private boolean hasNextPage;

    public PageDataResult(boolean success) {
        super(success);
    }

    public PageDataResult(boolean success, String message) {
        super(success, message);
    }

    public PageDataResult(boolean success, T data, boolean hasNextPage) {
        super(success, data);
        this.hasNextPage = hasNextPage;
    }

    public PageDataResult(boolean success, String message, T data, boolean hasNextPage) {
        super(success, message, data);
        this.hasNextPage = hasNextPage;
    }

    public boolean hasNextPage() {
        return hasNextPage;
    }

}