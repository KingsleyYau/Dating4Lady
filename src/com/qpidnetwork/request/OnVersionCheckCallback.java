package com.qpidnetwork.request;

import com.qpidnetwork.request.item.VersionCheckItem;

public interface OnVersionCheckCallback {
	public void OnVersionCheck(boolean isSuccess, String errno, String errmsg, VersionCheckItem item);
}
