/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom;

/**
 * @author liyalong
 * @version AdminChatroomListFragment.java, v 0.1 2022年11月09日 10:47 liyalong
 */
public class AdminChatroomListFragment extends BaseChatroomListFragment {

    @Override
    protected void loadChatroomList(int pageIndex) {
        chatroomViewModel.queryAdminChatroomList(pageIndex);
    }
}