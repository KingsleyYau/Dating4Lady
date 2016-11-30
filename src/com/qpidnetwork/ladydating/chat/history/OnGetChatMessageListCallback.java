package com.qpidnetwork.ladydating.chat.history;

import java.util.List;

import com.qpidnetwork.livechat.LCMessageItem;

public interface OnGetChatMessageListCallback {
	public void OnGetChatMessageList(boolean isSuccess, String errno, String errmsg, List<LCMessageItem> msgList);
}
