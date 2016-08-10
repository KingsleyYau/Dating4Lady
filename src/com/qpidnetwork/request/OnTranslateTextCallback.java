package com.qpidnetwork.request;

public interface OnTranslateTextCallback {
	public void OnTranslateText(long requestId, boolean isSuccess, String text);
}
