/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.model;

/**
 * 长连接结果模型
 *
 * @author liyalong
 * @version ConnectionResult.java, v 0.1 2022年07月29日 14:18 liyalong
 */
public class ConnectionResult {

    private Event event;

    private String message;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public enum Event {
        CONNECT_OK,
        RECONNECT_OK,
        DISCONNECT,
        EXCEPTION,
        KICKED
    }


}