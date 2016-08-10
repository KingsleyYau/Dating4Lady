package com.qpidnetwork.request;


/**
 * 5.14.	获取微视频图片
 */
public interface OnLCGetVideoPhotoCallback {
	public void OnLCGetVideoPhoto(long requestId, boolean isSuccess, String errno, String errmsg, String videoId, String filePath);
}
