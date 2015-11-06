package com.qpidnetwork.request;

import com.qpidnetwork.request.item.ManRecentViewListItem;

public interface OnQueryManRecentViewListCallback {
	public void OnQueryManRecentViewList(boolean isSuccess, String errno, String errmsg, ManRecentViewListItem[] itemList);
}
