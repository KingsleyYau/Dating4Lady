package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LCChatListItem;

public interface OnLCGetChatListCallback {
	public void OnLCGetChatList(boolean isSuccess, String errno, String errmsg, LCChatListItem[] list);
}
