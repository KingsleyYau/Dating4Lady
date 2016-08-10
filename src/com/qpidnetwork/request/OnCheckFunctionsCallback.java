package com.qpidnetwork.request;


public interface OnCheckFunctionsCallback {
	public void OnCheckFunctions(boolean isSuccess, String errno, String errmsg, int[] flags);
}
