/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.model;

/**
 * livedata通用结果模型
 *
 * @author liyalong
 * @version LoginResult.java, v 0.1 2022年07月28日 21:48 liyalong
 */
public class LiveDataResult<T> {

    private boolean success;

    private String message;

    private T data;

    public LiveDataResult(boolean success) {
        this.success = success;
    }

    public LiveDataResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LiveDataResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public LiveDataResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}