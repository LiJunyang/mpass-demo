/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.model;


/**
 * @author liyalong
 * @version ChatroomNotifyResult.java, v 0.1 2022年11月09日 11:17 liyalong
 */
public class ChatroomNotifyResult {

    private Operation operation;

    private String userId;

    public ChatroomNotifyResult(Operation operation, String userId) {
        this.operation = operation;
        this.userId = userId;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public enum Operation {
        JOIN_CHATROOM,
        LEAVE_CHATROOM,
        KICK_CHATROOM,
        DELETE_CHATROOM
    }

}