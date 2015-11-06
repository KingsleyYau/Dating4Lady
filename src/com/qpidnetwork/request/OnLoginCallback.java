package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LoginItem;

public interface OnLoginCallback {
	public void OnLogin(boolean isSuccess, String errno, String errmsg, LoginItem item);
}
