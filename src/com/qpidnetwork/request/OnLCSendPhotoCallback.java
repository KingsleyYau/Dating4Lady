package com.qpidnetwork.request;


/**
 * LiveChat发送私密照片
 */
public interface OnLCSendPhotoCallback {
	public void OnLCSendPhoto(long requestId, boolean isSuccess, String errno, String errmsg, String sendId);
}
