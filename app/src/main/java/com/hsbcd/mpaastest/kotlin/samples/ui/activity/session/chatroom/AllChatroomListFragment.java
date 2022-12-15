/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom;

/**
 * @author liyalong
 * @version AllChatroomListFragment.java, v 0.1 2022年11月09日 10:47 liyalong
 */
public class AllChatroomListFragment extends BaseChatroomListFragment {

    @Override
    protected void loadChatroomList(int pageIndex) {
        chatroomViewModel.queryAllChatroomList(pageIndex);
    }
}