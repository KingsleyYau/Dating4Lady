package com.qpidnetwork.request;

public interface OnRequestCallback {
	public void OnRequest(boolean isSuccess, String errno, String errmsg);
}
