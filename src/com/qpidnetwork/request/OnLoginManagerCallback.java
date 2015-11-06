package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LoginItem;

public interface OnLoginManagerCallback {
	public void OnLogin(boolean isSuccess, String errno, String errmsg, LoginItem item);
	public void OnLogout(int type);
}
