package com.qpidnetwork.request;

import com.qpidnetwork.request.item.ManRecentChatListItem;

public interface OnQueryManRecentChatListCallback {
	public void OnQueryManRecentChatList(boolean isSuccess, String errno, String errmsg, ManRecentChatListItem[] itemList, int totalCount);
}
