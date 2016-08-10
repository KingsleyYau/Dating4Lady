package com.qpidnetwork.request;
/**
 *	@author Yanni
 * 
 *	@version 2016-6-23
 */
public interface OnSaveAlbumPhotoCallback {
	public void onSaveAlbumPhoto(boolean isSuccess, String errno, String errmsg, String photoId);
}
