package com.qpidnetwork.request;


/**
 * 5.17.	发送微视频
 */
public interface OnLCSendVideoCallback {
	public void OnLCSendVideo(long requestId, boolean isSuccess, String errno, String errmsg, String sendId);
}
