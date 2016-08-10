package com.qpidnetwork.request;

import com.qpidnetwork.request.item.MyProfileItem;

public interface OnQueryMyProfileCallback {
	public void OnQueryMyProfileDetail(long requestId, boolean isSuccess, String errno, String errmsg, MyProfileItem item);
}
