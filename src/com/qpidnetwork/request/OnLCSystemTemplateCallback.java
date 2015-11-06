package com.qpidnetwork.request;


public interface OnLCSystemTemplateCallback {
	public void onSystemTemplate(boolean isSuccess, String errno, String errmsg, String[] tempList);
}
