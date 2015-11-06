package com.qpidnetwork.request;

import com.qpidnetwork.request.item.ManListItem;

public interface OnQueryManListCallback {
	public void OnQueryManList(boolean isSuccess, String errno, String errmsg, ManListItem[] itemList, int totalCount);
}
