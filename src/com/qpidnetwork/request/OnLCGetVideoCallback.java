package com.qpidnetwork.request;


/**
 * 5.15.	获取微视频文件URL
 */
public interface OnLCGetVideoCallback {
	public void OnLCGetVideo(long requestId, boolean isSuccess, String errno, String errmsg, String videoUrl);
}
