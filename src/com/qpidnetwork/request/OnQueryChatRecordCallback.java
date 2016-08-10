package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LCRecord;

public interface OnQueryChatRecordCallback {
	public void OnQueryChatRecord(boolean isSuccess, String errno, String errmsg, int dbTime, LCRecord[] recordList, String inviteId);
}
