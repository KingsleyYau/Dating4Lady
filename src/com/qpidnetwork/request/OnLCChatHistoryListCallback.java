package com.qpidnetwork.request;

import com.qpidnetwork.request.item.ChatHistoryListItem;

public interface OnLCChatHistoryListCallback {
	public void OnLCChatHistoryList(boolean isSuccess, String errno, String errmsg, ChatHistoryListItem[] chatHistoryList);
}
