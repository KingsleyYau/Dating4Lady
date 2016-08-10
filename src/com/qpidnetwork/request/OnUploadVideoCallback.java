package com.qpidnetwork.request;


public interface OnUploadVideoCallback {
	public void OnUploadVideo(boolean isSuccess, String errno,String errmsg, String identifyValues);
}
