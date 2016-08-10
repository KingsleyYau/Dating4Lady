package com.qpidnetwork.request;


/**
 * 下载文件
 */
public interface OnRequestFileCallback {
	public void OnRequestFile(long requestId, boolean isSuccess, String errno, String errmsg, String filePath);
}
