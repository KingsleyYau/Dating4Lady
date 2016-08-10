package com.qpidnetwork.request;
/**
 *	@author Yanni
 * 
 *	@version 2016-6-27
 */
public interface OnSaveAlbumVideoCallback {
	public void onSaveAlbumVideo(boolean isSuccess, String errno, String errmsg, String videoId);
}
