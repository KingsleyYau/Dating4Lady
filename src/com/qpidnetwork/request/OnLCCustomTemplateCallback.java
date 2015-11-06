package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LiveChatInviteTemplateListItem;

public interface OnLCCustomTemplateCallback {
	public void onCustomTemplate(boolean isSuccess, String errno, String errmsg, LiveChatInviteTemplateListItem[] tempList);
}
